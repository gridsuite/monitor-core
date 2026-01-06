package org.gridsuite.process.orchestrator.server.repositories;

import org.gridsuite.process.orchestrator.server.entities.ProcessExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessExecutionRepository extends JpaRepository<ProcessExecutionEntity, UUID> {
}
