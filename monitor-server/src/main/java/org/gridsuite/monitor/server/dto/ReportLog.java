/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Getter
@Setter
public class ReportLog {
    private String message;
    private Severity severity;
    private int depth;
    private UUID parentId;

    @JsonCreator
    public ReportLog(String message, Severity severity, int depth, UUID parentId) {
        this.message = message;
        this.severity = severity;
        this.depth = depth;
        this.parentId = parentId;
    }
}
