package org.opennms.drift.e2e;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.DoneableConfigMap;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.opennms.gizmo.k8s.GizmoK8sStack;
import org.opennms.gizmo.k8s.GizmoK8sStacker;
import org.opennms.gizmo.k8s.stacks.YamlBasedK8sStack;
import org.opennms.gizmo.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class MinionStack extends YamlBasedK8sStack {

    private static final Logger LOG = LoggerFactory.getLogger(MinionStack.class);

    private final String instanceId;

    public MinionStack(String instanceId) {
        super(Resources.getResource("minion.yaml"));
        useTemplating(true);
        this.instanceId = Objects.requireNonNull(instanceId);
    }

    @Override
    public List<GizmoK8sStack> getDependencies() {
        return Lists.newArrayList(new BurrowStack(), new OpenNMSStack());
    }

    @Override
    public List<ConfigMap> getConfigMaps(GizmoK8sStacker stacker) {
        final Map<String, String> data = Maps.newHashMap();
        data.put("org.opennms.core.ipc.sink.kafka.cfg", "bootstrap.servers=kafka-hs:9092\n" +
                "acks=1");

        data.put("org.opennms.features.telemetry.listeners-udp-8877.cfg", "name=Netflow-5\n" +
                "class-name=org.opennms.netmgt.telemetry.listeners.udp.UdpListener\n" +
                "batch.size=10\n" +
                "listener.port=8877");

        final ConfigMap configMap = new ConfigMapBuilder()
                .withNewMetadata()
                .withName("minion-config")
                .withNamespace(stacker.getNamespace())
                .endMetadata()
                .withData(data)
                .build();

        List<ConfigMap> configMaps = Lists.newArrayList();
        configMaps.addAll(super.getConfigMaps(stacker));
        configMaps.add(configMap);
        return configMaps;
    }

    @Override
    public Map<String, Object> getTemplateContext() {
        return ImmutableMap.of("instanceId", instanceId);
    }

    @Override
    public List<Consumer<GizmoK8sStacker>> getWaitingRules() {
        return ImmutableList.of((stacker) -> waitForMinion(stacker));
    }

    public static void waitForMinion(GizmoK8sStacker stacker) {
        final InetSocketAddress sshAddr = PodUtils.waitForPodAndForwardPort(stacker, "app", "minion", 8201);
        LOG.info("Waiting for Minion Karaf Shell @ {}.", sshAddr);
        await().atMost(2, MINUTES).pollInterval(5, SECONDS).pollDelay(0, SECONDS)
            .until(SshClient.canConnectViaSsh(sshAddr, "admin", "admin"));
        LOG.info("Minion's Karaf Shell is online.");
    }

}
