/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.core;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import lombok.Getter;
import lombok.Setter;
import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.ResultInfos;
import org.gridsuite.monitor.worker.server.dto.ReportInfos;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Getter
public class ProcessStepExecutionContext<C extends ProcessConfig> {

    private final UUID stepExecutionId = UUID.randomUUID();
    private final UUID previousStepExecutionId;
    private final ProcessExecutionContext<C> processContext;
    private final C config;
    private final ReportInfos reportInfos;
    private final Instant startedAt = Instant.now();
    private final ProcessStepType processStepType;

    @Setter
    private ResultInfos resultInfos;

    public ProcessStepExecutionContext(ProcessExecutionContext<C> processContext, C config, ProcessStepType processStepType, UUID previousStepExecutionId) {
        this.processContext = processContext;
        this.config = config;
        this.processStepType = processStepType;
        this.reportInfos = new ReportInfos(UUID.randomUUID(), ReportNode.newRootReportNode()
                .withAllResourceBundlesFromClasspath()
                .withMessageTemplate("process.worker.server.stepType")
                .withUntypedValue("stepType", processStepType.getName())
                .build());
        this.previousStepExecutionId = previousStepExecutionId;
    }

    public UUID getProcessExecutionId() {
        return processContext.getExecutionId();
    }

    public Network getNetwork() {
        return processContext.getNetwork();
    }

    public void setNetwork(Network network) {
        processContext.setNetwork(network);
    }
}
