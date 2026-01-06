package org.gridsuite.process.worker.server.processes.securityanalysis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gridsuite.process.worker.server.core.ProcessStepType;

@Getter
@RequiredArgsConstructor
public enum SAStepTypes implements ProcessStepType {
    APPLY_MODIFICATIONS("APPLY_MODIFICATIONS"),
    RUN_SA_COMPUTATION("RUN_SA_COMPUTATION");

    private final String name;
}
