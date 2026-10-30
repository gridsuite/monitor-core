/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.process.shortcircuit.steps;

import com.powsybl.commons.PowsyblException;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.shortcircuit.Fault;
import com.powsybl.shortcircuit.ShortCircuitAnalysis;
import com.powsybl.shortcircuit.ShortCircuitAnalysisResult;
import com.powsybl.shortcircuit.ShortCircuitParameters;
import lombok.Getter;
import org.gridsuite.monitor.commons.types.processconfig.ShortCircuitConfig;
import org.gridsuite.monitor.commons.types.result.ResultInfos;
import org.gridsuite.monitor.commons.types.result.ResultType;
import org.gridsuite.monitor.worker.server.clients.ShortCircuitRestClient;
import org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.core.process.AbstractProcessStep;
import org.gridsuite.monitor.worker.server.dto.parameters.shortcircuit.ShortCircuitParametersInfos;
import org.gridsuite.monitor.worker.server.orchestrator.TaskManager;
import org.gridsuite.monitor.worker.server.process.shortcircuit.ShortCircuitStepType;
import org.gridsuite.monitor.worker.server.services.FileService;
import org.gridsuite.monitor.worker.server.services.S3Service;
import org.gridsuite.monitor.worker.server.services.ShortCircuitParametersService;
import org.gridsuite.monitor.worker.server.utils.S3PathResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@Component
public class ShortCircuitRunComputationStep extends AbstractProcessStep<ShortCircuitConfig> {
    private final ShortCircuitParametersService shortCircuitParametersService;
    private final ShortCircuitRestClient shortCircuitRestClient;
    private final S3Service s3Service;
    private final FileService fileService;
    private final TaskManager taskManager;
    @Getter
    private final String defaultProvider;

    private static final String DEBUG_FILENAME_PREFIX = "debug";

    public ShortCircuitRunComputationStep(
        ShortCircuitParametersService shortCircuitParametersService,
        ShortCircuitRestClient shortCircuitRestClient,
        S3Service s3Service,
        FileService fileService,
        TaskManager taskManager,
        @Value("${shortcircuit-analysis.default-provider}") String defaultProvider) {
        super(ShortCircuitStepType.RUN_SC_COMPUTATION);
        this.shortCircuitParametersService = shortCircuitParametersService;
        this.shortCircuitRestClient = shortCircuitRestClient;
        this.s3Service = s3Service;
        this.fileService = fileService;
        this.taskManager = taskManager;
        this.defaultProvider = defaultProvider;
    }

    @Override
    public void execute(ProcessStepExecutionContext<ShortCircuitConfig> context) {
        Objects.requireNonNull(context.getNetwork());

        ShortCircuitParametersInfos parametersInfos = shortCircuitRestClient.getParameters(context.getConfig().shortCircuitParametersUuid());
        // TODO: in shortcircuit-server, retrieved parameters are post processed before being passed to the short-circuit calculation

        String provider = parametersInfos.getProvider() != null ? parametersInfos.getProvider() : getDefaultProvider();
        Map<String, String> specificParameters = parametersInfos.getSpecificParametersPerProvider().get(provider);
        ShortCircuitParameters commonParameters = shortCircuitParametersService.buildParameters(parametersInfos, provider, specificParameters);
        if (context.getDebugFileLocation() == null) {
            runComputation(context, provider, commonParameters, specificParameters);
        } else {
            runComputationWithDebug(context, provider, commonParameters, specificParameters);
        }
    }

    private void runComputationWithDebug(ProcessStepExecutionContext<ShortCircuitConfig> context, String provider,
                                        ShortCircuitParameters parameters, Map<String, String> specificParameters) {
        fileService.withDebugDir(context.getProcessStepType(), debugDir -> {
            try {
                parameters.setDebugDir(debugDir.toString());
                runComputation(context, provider, parameters, specificParameters);
            } finally {
                saveDebugFiles(context, debugDir);
            }
        });
    }

    private void runComputation(ProcessStepExecutionContext<ShortCircuitConfig> context, String provider,
                                ShortCircuitParameters parameters, Map<String, String> specificParameters) {
        List<Fault> faults = shortCircuitParametersService.getAllBusFaults(context.getNetwork(), specificParameters);
        shortCircuitParametersService.checkInconsistentVoltageLevels(context.getNetwork(), context.getReportNode());

        ShortCircuitAnalysisResult result = taskManager.runComputation(context.getProcessExecutionId(),
                () -> ShortCircuitAnalysis.find(provider).runAsync(
                        context.getNetwork(),
                        faults,
                        parameters,
                        LocalComputationManager.getDefault(),
                        List.of(),
                        context.getReportNode()));

        ResultInfos resultInfos = new ResultInfos(UUID.randomUUID(), ResultType.SHORT_CIRCUIT);
        shortCircuitRestClient.saveResult(resultInfos.resultUUID(), result);
        context.setResultInfos(resultInfos);
    }

    protected void saveDebugFiles(ProcessStepExecutionContext<ShortCircuitConfig> context, Path debugDir) {
        String s3FileName = DEBUG_FILENAME_PREFIX + ".zip";
        Path debugParentDir = debugDir.getParent();
        Path debugDirPath = debugParentDir.resolve(debugDir.getFileName().toString());
        String s3Key = S3PathResolver.getProcessStepDebugFilePath(
            context.getDebugFileLocation(),
            context.getProcessStepType().getName(),
            context.getStepOrder(),
            s3FileName);

        try {
            s3Service.compressAndUploadToS3(debugDirPath, s3Key, s3FileName);
        } catch (IOException e) {
            throw new PowsyblException("An error occurred while saving debug file: " + e.getCause(), e);
        }
    }
}
