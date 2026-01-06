package org.gridsuite.process.commons;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessExecutionStatusUpdate {
    private ProcessStatus status;
    private String executionEnvName;
    private Instant completedAt;
}
