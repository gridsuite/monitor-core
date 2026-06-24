/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons.error;

import com.powsybl.ws.commons.error.AbstractBusinessException;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Getter
public class MonitorException extends AbstractBusinessException {
    private final MonitorBusinessErrorCode errorCode;
    private final transient Map<String, Object> businessErrorValues;

    public MonitorException(MonitorBusinessErrorCode errorCode, String message) {
        this(errorCode, message, null, null);
    }

    public MonitorException(MonitorBusinessErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, null, cause);
    }

    public MonitorException(MonitorBusinessErrorCode errorCode, String message, Map<String, Object> businessErrorValues) {
        this(errorCode, message, businessErrorValues, null);
    }

    public MonitorException(MonitorBusinessErrorCode errorCode, String message, Map<String, Object> businessErrorValues, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.businessErrorValues = businessErrorValues != null ? Map.copyOf(businessErrorValues) : Map.of();
    }

    @NonNull
    @Override
    public MonitorBusinessErrorCode getBusinessErrorCode() {
        return errorCode;
    }
}
