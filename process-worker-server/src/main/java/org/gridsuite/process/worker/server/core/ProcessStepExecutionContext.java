package org.gridsuite.process.worker.server.core;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import lombok.Getter;
import lombok.Setter;
import org.gridsuite.process.commons.ProcessConfig;
import org.gridsuite.process.commons.ResultInfos;
import org.gridsuite.process.worker.server.dto.ReportInfos;

import java.time.Instant;
import java.util.UUID;

@Getter
public class ProcessStepExecutionContext<C extends ProcessConfig> {

    private final UUID stepExecutionId = UUID.randomUUID();
    private final ProcessExecutionContext<C> processContext;
    private final C config;
    @Setter
    private ResultInfos resultInfos;
    @Setter
    private ReportInfos reportInfos;
    @Getter
    private final Instant startedAt = Instant.now();
    @Getter
    private final ProcessStepType processStepType;

    public ProcessStepExecutionContext(ProcessExecutionContext<C> processContext, C config, ProcessStepType processStepType) {
        this.processContext = processContext;
        this.config = config;
        this.processStepType = processStepType;
        this.reportInfos = new ReportInfos(UUID.randomUUID(), ReportNode.newRootReportNode()
                .withAllResourceBundlesFromClasspath()
                .withMessageTemplate("process.worker.server.stepType")
                .withUntypedValue("stepType", processStepType.getName())
                .build());
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
