package org.gridsuite.process.orchestrator.server.entities;

import jakarta.persistence.*;
import lombok.*;
import org.gridsuite.process.commons.ProcessStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "processExecution")
public class ProcessExecutionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column
    private String type;

    @Column
    private UUID caseUuid;

    @Column
    @Enumerated(EnumType.STRING)
    private ProcessStatus status;

    @Column
    private String executionEnvName;

    @Column
    private Instant scheduledAt;

    @Column
    private Instant completedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "execution_id", foreignKey = @ForeignKey(name = "processExecutionStep_processExecution_fk"))
    private List<ProcessExecutionStepEntity> steps = new ArrayList<>();
}
