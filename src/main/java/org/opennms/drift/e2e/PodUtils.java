package org.opennms.drift.e2e;

import io.fabric8.kubernetes.api.model.Pod;
import org.opennms.gizmo.k8s.GizmoK8sStacker;
import org.opennms.gizmo.k8s.utils.StackUtils;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class PodUtils {

    public static InetSocketAddress waitForPodAndForwardPort(GizmoK8sStacker stacker,
            String labelKey, String labelValue, int port) {
        return waitForPodAndForwardPort(stacker, labelKey, labelValue, port, 2, MINUTES);
    }

    public static InetSocketAddress waitForPodAndForwardPort(GizmoK8sStacker stacker,
                                                             String labelKey, String labelValue, int port, long timeout, TimeUnit unit) {
        await().atMost(timeout, unit).pollInterval(5, SECONDS).pollDelay(0, SECONDS)
                .until(() -> StackUtils.getFirstReadyPod(stacker, labelKey, labelValue), is(notNullValue()));
        final Pod pod = StackUtils.getFirstReadyPod(stacker, labelKey, labelValue);
        return stacker.portForward(pod.getMetadata().getName(), port);
    }

}
