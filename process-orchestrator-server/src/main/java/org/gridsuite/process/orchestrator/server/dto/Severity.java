/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.process.orchestrator.server.dto;

public enum Severity {
    UNKNOWN(0),
    TRACE(1),
    DEBUG(2),
    DETAIL(3),
    INFO(4),
    WARN(5),
    ERROR(6),
    FATAL(7);

    private final int level;

    Severity(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public static Severity fromValue(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        try {
            return valueOf(value);
        } catch (final IllegalArgumentException | NullPointerException e) {
            return UNKNOWN;
        }
    }
}
