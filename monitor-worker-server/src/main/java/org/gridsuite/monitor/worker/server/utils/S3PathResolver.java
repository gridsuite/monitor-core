/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.utils;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
public final class S3PathResolver {
    private S3PathResolver() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String getProcessStepDebugFilePath(String debugFileLocation, String processStepType, Integer stepOrder, String fileName) {
        String s3Delimiter = "/";
        return String.join(s3Delimiter,
            debugFileLocation,
            processStepType + "_" + stepOrder,
            fileName);
    }
}
