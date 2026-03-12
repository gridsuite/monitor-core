/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.process.commons.steps;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gridsuite.monitor.worker.server.core.process.ProcessStepType;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Getter
@RequiredArgsConstructor
public enum CommonStepType implements ProcessStepType {
    LOAD_NETWORK("LOAD_NETWORK"),
    APPLY_MODIFICATIONS("APPLY_MODIFICATIONS");

    private final String name;
}
