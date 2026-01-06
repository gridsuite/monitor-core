package org.gridsuite.process.worker.server.processes.commons.steps;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import org.gridsuite.process.commons.ProcessConfig;
import org.gridsuite.process.worker.server.core.AbstractProcessStep;
import org.gridsuite.process.worker.server.core.ProcessStepExecutionContext;
import org.gridsuite.process.worker.server.services.NetworkConversionService;

import java.util.UUID;

public class LoadNetworkStep<C extends ProcessConfig> extends AbstractProcessStep<C> {

    private final NetworkConversionService networkConversionService;

    public LoadNetworkStep(NetworkConversionService networkConversionService) {
        super(CommonStepTypes.LOAD_NETWORK);
        this.networkConversionService = networkConversionService;
    }

    @Override
    public void execute(ProcessStepExecutionContext<C> context) {
        UUID caseId = context.getConfig().caseUuid();
        Network network = loadNetworkFromCase(caseId, context.getReportInfos().reportNode());
        context.setNetwork(network);
    }

    private Network loadNetworkFromCase(UUID caseUuid, ReportNode reportNode) {
        ReportNode reporter = reportNode.newReportNode()
                    .withMessageTemplate("process.worker.server.importCase")
                    .withUntypedValue("caseUuid", caseUuid.toString())
                    .add();
        return networkConversionService.createNetwork(caseUuid, reporter);
    }
}
