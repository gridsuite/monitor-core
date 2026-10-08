/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.openloadflow.OpenLoadFlowParameters;
import org.gridsuite.monitor.worker.server.dto.parameters.loadflow.LoadFlowParametersInfos;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Antoine Bouhours {@literal <antoine.bouhours at rte-france.com>}
 */
class LoadFlowParametersServiceTest {

    @Test
    void appliesSavedProviderParametersToCommonParameters() {
        LoadFlowParameters commonParameters = LoadFlowParameters.load();
        LoadFlowParametersInfos infos = LoadFlowParametersInfos.builder()
                .provider("OpenLoadFlow")
                .commonParameters(commonParameters)
                .specificParametersPerProvider(Map.of("OpenLoadFlow", Map.of("plausibleActivePowerLimit", "5000.0")))
                .build();

        LoadFlowParameters parameters = LoadFlowParametersService.buildParameters(infos);

        assertThat(parameters).isSameAs(commonParameters);
        assertThat(parameters.getExtension(OpenLoadFlowParameters.class).getPlausibleActivePowerLimit()).isEqualTo(5000.0);
    }

    @Test
    void missingCommonParametersLoadPlatformDefaults() {
        LoadFlowParametersInfos infos = LoadFlowParametersInfos.builder().build();

        LoadFlowParameters parameters = LoadFlowParametersService.buildParameters(infos);

        assertThat(parameters).usingRecursiveComparison().isEqualTo(LoadFlowParameters.load());
    }
}
