/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons.utils;

import java.util.UUID;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
public final class S3PathUtils {
    public static final String S3_DELIMITER = "/";

    private S3PathUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Builds root path used to build debug file location
     * @param executionEnvName
     * @param processType
     * @param executionId
     * @return {executionEnvName}_debug/process/{processType}/{executionId}
     */
    public static String toDebugLocation(String executionEnvName, String processType, UUID executionId) {
        return String.join(S3_DELIMITER, executionEnvName + "_debug", "process", processType, executionId.toString());
    }
}
