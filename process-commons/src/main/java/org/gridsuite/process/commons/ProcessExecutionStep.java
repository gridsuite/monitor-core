package org.gridsuite.process.commons;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessExecutionStep {
    private UUID id;
    private String stepType;
    private UUID previousStepId;
    private StepStatus status;
    private UUID resultId;
    private ResultType resultType;
    private UUID reportId;
    private Instant startedAt;
    private Instant completedAt;
}
