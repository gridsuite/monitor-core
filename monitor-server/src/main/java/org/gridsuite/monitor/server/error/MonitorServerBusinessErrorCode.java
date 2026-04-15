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
    DOWNLOAD_DEBUG_FILE_ERROR("monitor.server.downloadDebugFileError"),
    UNSUPPORTED_PROCESS_CONFIG_TYPE("monitor.server.unsupportedProcessConfigType"),
    PROCESS_CONFIG_TYPE_MISMATCH("monitor.server.processConfigTypeMismatch"),
    UNSUPPORTED_PROCESS_CONFIG_ENTITY_TYPE("monitor.server.unsupportedProcessConfigEntityType"),
    DIFFERENT_PROCESS_CONFIG_TYPE("monitor.server.differentProcessConfigType"),
    UNSUPPORTED_RESULT_TYPE("monitor.server.unsupportedResultType"),
    PARSING_MESSAGE_PAYLOAD_ERROR("monitor.server.messagePayloadParsingError");

    private final String code;

    MonitorServerBusinessErrorCode(String code) {
        this.code = code;
    }

    public String value() {
        return code;
    }
}
