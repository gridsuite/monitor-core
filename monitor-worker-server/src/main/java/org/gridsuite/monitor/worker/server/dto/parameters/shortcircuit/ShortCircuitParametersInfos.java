/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.dto.parameters.shortcircuit;

import com.powsybl.shortcircuit.ShortCircuitParameters;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ShortCircuitParametersInfos {
    String provider;
    ShortCircuitPredefinedConfiguration predefinedParameters;
    ShortCircuitParameters commonParameters;
    Map<String, Map<String, String>> specificParametersPerProvider;
}
