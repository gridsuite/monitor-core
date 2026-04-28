/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processexecution;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
public record ProcessCreationResult(
    String debugLocationFile,
    ProcessConfig processConfig
) {
}
