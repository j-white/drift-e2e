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

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.gizmo.k8s.GizmoK8sRule;
import org.opennms.gizmo.k8s.GizmoK8sStacker;
import org.opennms.gizmo.k8s.stacks.HelmChartBasedStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.Callable;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class DriftHelmStackTest {

    private static final Logger LOG = LoggerFactory.getLogger(DriftHelmStackTest.class);

    @Rule
    public GizmoK8sRule gizmo = GizmoK8sRule.builder()
            .withStack(new HelmChartBasedStack("drift", "/home/jesse/git/opennms-k8s-charts/drift"))
            .skipTearDown(true)
            .build();

    @Test
    public void canBootstrap() {
        GizmoK8sStacker stacker = gizmo.getStacker();
        final InetSocketAddress kafkaAddr = PodUtils.waitForPodAndForwardPort(stacker, "app", "kafka", 9092);
        LOG.info("Waiting for Kafka @ {}.", kafkaAddr);
        await().atMost(2, MINUTES).pollInterval(5, SECONDS).pollDelay(0, SECONDS)
                .until(canListTopics(kafkaAddr));
        LOG.info("Kafka is online.");
    }

    public static Callable<Boolean> canListTopics(final InetSocketAddress addr) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                LOG.info("Attempting list topics on {}:{}", addr.getHostString(), addr.getPort());
                try {
                    final Properties props = new Properties();
                    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, String.format("%s:%d", addr.getHostString(), addr.getPort()));
                    props.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaTestConsumer");
                    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getCanonicalName());
                    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getCanonicalName());

                    final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
                    consumer.listTopics();
                    return true;
                } catch (Exception e) {
                    LOG.debug("Listing topics failed: " + e.getMessage());
                    return false;
                }
            }
        };
    }
}
