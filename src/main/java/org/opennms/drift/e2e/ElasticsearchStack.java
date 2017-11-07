/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.drift.e2e;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.indices.aliases.GetAliases;
import org.opennms.gizmo.k8s.GizmoK8sStack;
import org.opennms.gizmo.k8s.GizmoK8sStacker;
import org.opennms.gizmo.k8s.stacks.YamlBasedK8sStack;
import org.opennms.gizmo.k8s.utils.StackUtils;
import org.opennms.gizmo.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class ElasticsearchStack extends YamlBasedK8sStack {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchStack.class);

    public ElasticsearchStack() {
        super(Resources.getResource("elasticsearch.yaml"));
    }

    @Override
    public List<GizmoK8sStack> getDependencies() {
        return Lists.newArrayList(new ElasticsearchMasterStack());
    }

    @Override
    public List<Consumer<GizmoK8sStacker>> getWaitingRules() {
        return ImmutableList.of((stacker) -> waitForElasticsearch(stacker));
    }

    public static void waitForElasticsearch(GizmoK8sStacker stacker) {
        final InetSocketAddress esAddr = PodUtils.waitForPodAndForwardPort(stacker, "app", "es-client", 9200);
        /*LOG.info("Waiting for Elasticsearch @ {}.", esAddr);
        await().atMost(2, MINUTES).pollInterval(5, SECONDS).pollDelay(0, SECONDS)
                .until(canConnectToEs(esAddr));
        LOG.info("Elasticsearch is online.");
        */
    }

    public static Callable<Boolean> canConnectToEs(final InetSocketAddress addr) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                LOG.info("Attempting to connect {}:{}", addr.getHostString(), addr.getPort());
                try {
                    // Construct a new Jest client according to configuration via factory
                    final JestClientFactory factory = new JestClientFactory();
                    factory.setHttpClientConfig(new HttpClientConfig
                            .Builder(String.format("http://%s:%d", addr.getHostString(), addr.getPort()))
                            .multiThreaded(true)
                            .build());
                    final JestClient client = factory.getObject();
                    final GetAliases aliases = new GetAliases.Builder().build();
                    final JestResult result = client.execute(aliases);
                    final String json  = result.getJsonString();
                    LOG.info("Got JSON: {}", json);
                    return true;
                } catch (Exception e) {
                    LOG.debug("Elasticsearch connection failed: " + e.getMessage());
                    return false;
                }
            }
        };
    }

}
