/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.repositories;

import org.gridsuite.monitor.commons.types.processexecution.ProcessStatus;
import org.gridsuite.monitor.server.entities.processexecution.ProcessExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Repository
public interface ProcessExecutionRepository extends JpaRepository<ProcessExecutionEntity, UUID> {
    List<ProcessExecutionEntity> findAllByScheduledAtIsNotNullOrderByScheduledAtDesc();

    @Modifying
    @Query("update ProcessExecutionEntity p set p.status = :newStatus where p.id = :id and p.status in :eligibleStatuses")
    int updateStatusIfEligible(UUID id, ProcessStatus newStatus, Collection<ProcessStatus> eligibleStatuses);
}
