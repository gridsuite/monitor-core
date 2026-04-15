/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.error;

import com.powsybl.ws.commons.error.PowsyblWsProblemDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.gridsuite.monitor.server.error.MonitorServerBusinessErrorCode.DOWNLOAD_DEBUG_FILE_ERROR;
import static org.gridsuite.monitor.server.error.MonitorServerBusinessErrorCode.UNSUPPORTED_PROCESS_CONFIG_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
class MonitorServerExceptionHandlerTest {
    private MonitorServerExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MonitorServerExceptionHandler(() -> "monitor-server");
    }

    @Test
    void mapsInternalErrorBusinessErrorToStatus() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/results-endpoint/uuid");
        MonitorServerException exception = new MonitorServerException(DOWNLOAD_DEBUG_FILE_ERROR, "An error occurred while downloading debug files");
        ResponseEntity<PowsyblWsProblemDetail> response = handler.handleMonitorServerException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertEquals("monitor.server.downloadDebugFileError", response.getBody().getBusinessErrorCode());
    }

    @Test
    void mapsBadRequestBusinessErrorToStatus() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/results-endpoint/uuid");
        MonitorServerException exception = new MonitorServerException(UNSUPPORTED_PROCESS_CONFIG_TYPE, "Unsupported process config type");
        ResponseEntity<PowsyblWsProblemDetail> response = handler.handleMonitorServerException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertEquals("monitor.server.unsupportedProcessConfigType", response.getBody().getBusinessErrorCode());
    }
}
