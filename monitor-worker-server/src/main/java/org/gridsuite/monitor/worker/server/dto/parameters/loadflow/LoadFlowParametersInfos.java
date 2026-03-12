/**
  Copyright (c) 2026, RTE (http://www.rte-france.com)
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.dto.parameters.loadflow;

import com.powsybl.loadflow.LoadFlowParameters;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.gridsuite.monitor.worker.server.dto.parameters.commons.LimitReductionsByVoltageLevel;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class LoadFlowParametersInfos {
    private UUID uuid;
    private String provider;
    private LoadFlowParameters commonParameters;
    private Map<String, Map<String, String>> specificParametersPerProvider;

    private List<LimitReductionsByVoltageLevel> limitReductions;

    private Float limitReduction; // Only for providers other than OpenLoadFlow
}
