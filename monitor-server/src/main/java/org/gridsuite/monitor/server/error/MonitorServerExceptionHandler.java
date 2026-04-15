/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.error;

import com.powsybl.ws.commons.error.AbstractBusinessExceptionHandler;
import com.powsybl.ws.commons.error.PowsyblWsProblemDetail;
import com.powsybl.ws.commons.error.ServerNameProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@ControllerAdvice
public class MonitorServerExceptionHandler extends AbstractBusinessExceptionHandler<MonitorServerException, MonitorServerBusinessErrorCode> {

    public MonitorServerExceptionHandler(ServerNameProvider serverNameProvider) {
        super(serverNameProvider);
    }

    @NonNull
    @Override
    protected MonitorServerBusinessErrorCode getBusinessCode(MonitorServerException ex) {
        return ex.getBusinessErrorCode();
    }

    @Override
    protected HttpStatus mapStatus(MonitorServerBusinessErrorCode errorCode) {
        return switch (errorCode) {
            case DOWNLOAD_DEBUG_FILE_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            case UNSUPPORTED_PROCESS_CONFIG_TYPE,
                 PROCESS_CONFIG_TYPE_MISMATCH,
                 UNSUPPORTED_PROCESS_CONFIG_ENTITY_TYPE,
                 DIFFERENT_PROCESS_CONFIG_TYPE,
                 UNSUPPORTED_RESULT_TYPE,
                 PARSING_MESSAGE_PAYLOAD_ERROR -> HttpStatus.BAD_REQUEST;
        };
    }

    @ExceptionHandler(MonitorServerException.class)
    protected ResponseEntity<PowsyblWsProblemDetail> handleMonitorServerException(
        MonitorServerException exception, HttpServletRequest request) {
        return super.handleDomainException(exception, request);
    }
}
