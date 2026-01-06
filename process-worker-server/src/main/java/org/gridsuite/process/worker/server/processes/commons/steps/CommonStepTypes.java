package org.gridsuite.process.worker.server.processes.commons.steps;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gridsuite.process.worker.server.core.ProcessStepType;

@Getter
@RequiredArgsConstructor
public enum CommonStepTypes implements ProcessStepType {
    LOAD_NETWORK("LOAD_NETWORK");

    private final String name;
}
