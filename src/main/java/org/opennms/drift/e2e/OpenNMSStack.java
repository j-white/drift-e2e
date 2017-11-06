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
import org.opennms.gizmo.k8s.GizmoK8sStack;
import org.opennms.gizmo.k8s.GizmoK8sStacker;
import org.opennms.gizmo.k8s.stacks.YamlBasedK8sStack;
import org.opennms.gizmo.utils.SshClient;
import org.postgresql.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class OpenNMSStack extends YamlBasedK8sStack {

    private static final Logger LOG = LoggerFactory.getLogger(PostgresStack.class);

    public OpenNMSStack() {
        super(Resources.getResource("opennms.yaml"));
    }

    @Override
    public List<GizmoK8sStack> getDependencies() {
        return Lists.newArrayList(new PostgresStack());
    }

    @Override
    public List<Consumer<GizmoK8sStacker>> getWaitingRules() {
        return ImmutableList.of((stacker) -> waitForOpenNMS(stacker));
    }

    public static void waitForOpenNMS(GizmoK8sStacker stacker) {
        final InetSocketAddress httpAddr = PodUtils.waitForPodAndForwardPort(stacker, "app", "opennms", 8980);
        final OpenNMSRestClient restClient = new OpenNMSRestClient(httpAddr);
        final Callable<String> getDisplayVersion = new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    return restClient.getDisplayVersion();
                } catch (Throwable t) {
                    LOG.debug("Version lookup failed: " + t.getMessage());
                    return null;
                }
            }
        };

        LOG.info("Waiting for OpenNMS REST service @ {}.", httpAddr);
        // TODO: It's possible that the OpenNMS server doesn't start if there are any
        // problems in $OPENNMS_HOME/etc. Instead of waiting the whole 5 minutes and timing out
        // we should also poll the status of the container, so we can fail sooner.
        await().atMost(5, MINUTES).pollInterval(10, SECONDS).pollDelay(0, SECONDS)
                .until(getDisplayVersion, is(notNullValue()));
        LOG.info("OpenNMS's REST service is online.");

        final InetSocketAddress sshAddr = PodUtils.waitForPodAndForwardPort(stacker, "app", "opennms", 8101);
        LOG.info("Waiting for OpenNMS Karaf Shell @ {}.", sshAddr);
        await().atMost(2, MINUTES).pollInterval(5, SECONDS)
                .until(SshClient.canConnectViaSsh(sshAddr, "admin", "admin"));
        LOG.info("OpenNMS's Karaf Shell is online.");
    }
}
