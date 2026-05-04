/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.error;

import com.powsybl.ws.commons.error.BusinessErrorCode;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public enum MonitorServerBusinessErrorCode implements BusinessErrorCode {
    DIFFERENT_PROCESS_CONFIG_TYPE("monitor.server.differentProcessConfigType");

    private final String code;

    MonitorServerBusinessErrorCode(String code) {
        this.code = code;
    }

    public String value() {
        return code;
    }
}
