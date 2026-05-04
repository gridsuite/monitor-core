/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.process.loadflow;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gridsuite.monitor.worker.server.core.process.ProcessStepType;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@Getter
@RequiredArgsConstructor
public enum LoadflowStepType implements ProcessStepType {
    RUN_LF_COMPUTATION("RUN_LF_COMPUTATION");

    private final String name;
}
