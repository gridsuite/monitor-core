/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.orchestrator.context;

import com.powsybl.commons.report.ReportNode;
import lombok.Getter;
import lombok.Setter;
import org.gridsuite.monitor.server.dto.report.ReportInfos;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.result.ResultInfos;
import org.gridsuite.monitor.server.orchestrator.process.ProcessStepType;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public class ProcessStepExecutionContext<C extends ProcessConfig> {

    private final ProcessExecutionContext<C> processContext;

    @Getter
    private final UUID stepExecutionId;
    @Getter
    private final int stepOrder;
    @Getter
    private final ReportInfos reportInfos;
    @Getter
    private final Instant startedAt = Instant.now();
    @Getter
    private final ProcessStepType processStepType;

    @Getter
    @Setter
    private ResultInfos resultInfos;

    public ProcessStepExecutionContext(ProcessExecutionContext<C> processContext, ProcessStepType processStepType, UUID stepId, int stepOrder) {
        this.processContext = processContext;
        this.stepExecutionId = stepId;
        this.processStepType = processStepType;
        this.reportInfos = new ReportInfos(UUID.randomUUID(), ReportNode.newRootReportNode()
                .withAllResourceBundlesFromClasspath()
                .withMessageTemplate("monitor.server.stepType")
                .withUntypedValue("stepType", processStepType.getName())
                .build());
        this.stepOrder = stepOrder;
    }

    public UUID getProcessExecutionId() {
        return processContext.getExecutionId();
    }

    public C getConfig() {
        return processContext.getConfig();
    }

    public UUID getCaseUuid() {
        return processContext.getCaseUuid();
    }

    public String getCaseS3Key() {
        return processContext.getCaseS3Key();
    }

    public void setCaseS3Key(String caseS3Key) {
        processContext.setCaseS3Key(caseS3Key);
    }

    public String getDebugFileLocation() {
        return processContext.getDebugFileLocation();
    }
}
