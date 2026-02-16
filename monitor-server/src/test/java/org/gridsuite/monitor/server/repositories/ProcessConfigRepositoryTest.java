/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.repositories;

import org.gridsuite.monitor.server.entities.SecurityAnalysisConfigEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@DataJpaTest
class ProcessConfigRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProcessConfigRepository processConfigRepository;

    @Test
    void saveSecurityAnalysisConfig() {
        UUID parametersUuid = UUID.randomUUID();
        List<String> contingencies = List.of("contingency1", "contingency2");
        List<UUID> modificationUuids = List.of(UUID.randomUUID(), UUID.randomUUID());
        Instant creationDate = Instant.now().minusSeconds(60);
        Instant lastModificationDate = Instant.now();

        SecurityAnalysisConfigEntity securityAnalysisConfig = SecurityAnalysisConfigEntity.builder()
            .parametersUuid(parametersUuid)
            .contingencies(contingencies)
            .modificationUuids(modificationUuids)
            .owner("user1")
            .creationDate(creationDate)
            .lastModificationDate(lastModificationDate)
            .lastModifiedBy("user2")
            .build();

        processConfigRepository.save(securityAnalysisConfig);
        // Force DB write and reload
        entityManager.flush();
        entityManager.clear();

        SecurityAnalysisConfigEntity retrieved = (SecurityAnalysisConfigEntity) processConfigRepository.findById(securityAnalysisConfig.getId()).orElseThrow();
        assertThat(retrieved.getParametersUuid()).isEqualTo(parametersUuid);
        assertThat(retrieved.getContingencies()).isEqualTo(contingencies);
        assertThat(retrieved.getModificationUuids()).isEqualTo(modificationUuids);
        assertThat(retrieved.getOwner()).isEqualTo("user1");
        assertThat(retrieved.getCreationDate().truncatedTo(ChronoUnit.MILLIS)).isEqualTo(creationDate.truncatedTo(ChronoUnit.MILLIS));
        assertThat(retrieved.getLastModificationDate().truncatedTo(ChronoUnit.MILLIS)).isEqualTo(lastModificationDate.truncatedTo(ChronoUnit.MILLIS));
        assertThat(retrieved.getLastModifiedBy()).isEqualTo("user2");
    }
}
