/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.repositories.automaticprocessconfig;

import jakarta.persistence.PersistenceException;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.entities.automaticprocessconfig.AutomaticProcessConfigEntity;
import org.gridsuite.monitor.server.entities.automaticprocessconfig.AutomaticProcessConfigHistoryEntity;
import org.gridsuite.monitor.server.entities.processconfig.LoadFlowConfigEntity;
import org.gridsuite.monitor.server.repositories.processconfig.LoadFlowConfigRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Radouane Khouadri <redouane.khouadri_externe at rte-france.com>
 */
@DataJpaTest
class AutomaticProcessConfigRepositoryTest {
    @Autowired
    private AutomaticProcessConfigRepository repository;
    @Autowired
    private LoadFlowConfigRepository loadFlowConfigRepository;
    @Autowired
    private AutomaticProcessConfigHistoryRepository historyRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void persistsCurrentConfigAndHistory() {
        LoadFlowConfigEntity processConfig = new LoadFlowConfigEntity();
        processConfig.setModifications(List.of());
        LoadFlowConfigEntity savedProcessConfig = loadFlowConfigRepository.save(processConfig);

        AutomaticProcessConfigEntity config = new AutomaticProcessConfigEntity();
        config.setProcessType(ProcessType.LOADFLOW);
        config.setConfigUuid(savedProcessConfig.getId());
        repository.save(config);

        AutomaticProcessConfigHistoryEntity history = new AutomaticProcessConfigHistoryEntity();
        history.setId(java.util.UUID.randomUUID());
        history.setProcessType(ProcessType.LOADFLOW);
        history.setConfigUuid(savedProcessConfig.getId());
        history.setChangedAt(Instant.parse("2026-10-30T10:00:00Z"));
        history.setChangedBy("admin");
        historyRepository.save(history);
        entityManager.flush();
        entityManager.clear();

        AutomaticProcessConfigEntity retrieved = repository.findById(ProcessType.LOADFLOW).orElseThrow();
        assertThat(retrieved.getConfigUuid()).isEqualTo(savedProcessConfig.getId());
        assertThat(historyRepository.findById(history.getId()))
            .hasValueSatisfying(savedHistory -> {
                assertThat(savedHistory.getConfigUuid()).isEqualTo(savedProcessConfig.getId());
                assertThat(savedHistory.getConfigName()).isNull();
                assertThat(savedHistory.getChangedBy()).isEqualTo("admin");
            });

        assertThatThrownBy(() -> entityManager.getEntityManager()
            .createNativeQuery("DELETE FROM process_config WHERE id = :id")
            .setParameter("id", savedProcessConfig.getId())
            .executeUpdate())
            .isInstanceOf(PersistenceException.class);
    }
}
