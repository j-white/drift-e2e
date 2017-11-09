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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.function.Consumer;

public class BurrowStack extends YamlBasedK8sStack {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaStack.class);

    public BurrowStack() {
        super(Resources.getResource("burrow.yaml"));
    }

    @Override
    public List<Consumer<GizmoK8sStacker>> getWaitingRules() {
        return ImmutableList.of((stacker) -> waitForBurrow(stacker));
    }

    @Override
    public List<GizmoK8sStack> getDependencies() {
        return Lists.newArrayList(new KafkaStack());
    }

    public static void waitForBurrow(GizmoK8sStacker stacker) {
        final InetSocketAddress burrowAddr = PodUtils.waitForPodAndForwardPort(stacker, "app", "burrow", 8000);
    }
}
