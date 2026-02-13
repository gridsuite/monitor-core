/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
class SecurityAnalysisConfigTest {

    @Test
    void createSecurityAnalysisConfig() {
        UUID parametersUuid = UUID.randomUUID();
        UUID modificationUuid = UUID.randomUUID();
        List<String> contingencies = List.of("contingency1", "contingency2");
        String owner = "user1";
        Instant creationDate = Instant.now();
        Instant lastModificationDate = Instant.now().plusSeconds(60);
        String lastModifiedBy = "user2";

        SecurityAnalysisConfig config = new SecurityAnalysisConfig(
            parametersUuid,
            contingencies,
            List.of(modificationUuid),
            owner,
            creationDate,
            lastModificationDate,
            lastModifiedBy
        );

        assertThat(config.getParametersUuid()).isEqualTo(parametersUuid);
        assertThat(config.getContingencies()).isEqualTo(contingencies);
        assertThat(config.getModificationUuids()).containsExactly(modificationUuid);
        assertThat(config.getOwner()).isEqualTo(owner);
        assertThat(config.getCreationDate()).isEqualTo(creationDate);
        assertThat(config.getLastModificationDate()).isEqualTo(lastModificationDate);
        assertThat(config.getLastModifiedBy()).isEqualTo(lastModifiedBy);
        assertThat(config.processType()).isEqualTo(ProcessType.SECURITY_ANALYSIS);
    }
}
