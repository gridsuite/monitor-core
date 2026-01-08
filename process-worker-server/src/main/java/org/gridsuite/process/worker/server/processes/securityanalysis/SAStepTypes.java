/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.process.worker.server.processes.securityanalysis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gridsuite.process.worker.server.core.ProcessStepType;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Getter
@RequiredArgsConstructor
public enum SAStepTypes implements ProcessStepType {
    APPLY_MODIFICATIONS("APPLY_MODIFICATIONS"),
    RUN_SA_COMPUTATION("RUN_SA_COMPUTATION");

    private final String name;
}
