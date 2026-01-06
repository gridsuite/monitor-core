package org.gridsuite.process.orchestrator.server.entities;

import jakarta.persistence.*;
import lombok.*;
import org.gridsuite.process.commons.ResultType;
import org.gridsuite.process.commons.StepStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "processExecutionStep")
public class ProcessExecutionStepEntity {

    //Id is generated in the worker
    @Id
    private UUID id;

    @Column
    private String stepType;

    @Column
    private UUID previousStepId;

    @Column
    @Enumerated(EnumType.STRING)
    private StepStatus status;

    @Column
    private UUID resultId;

    @Column
    @Enumerated(EnumType.STRING)
    private ResultType resultType;

    @Column
    private UUID reportId;

    @Column
    private Instant startedAt;

    @Column
    private Instant completedAt;
}
