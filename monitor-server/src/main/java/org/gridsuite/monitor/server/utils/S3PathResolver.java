/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
@Service
public final class S3PathResolver {
    private final String s3RootPath;

    public S3PathResolver(@Value("${powsybl-ws.s3.subpath.prefix:}${debug-subpath:debug}") String s3RootPath) {
        this.s3RootPath = s3RootPath;
    }

    /**
     * Builds root path used to build debug file location
     * @param processType
     * @param executionId
     * @return {executionEnvName}_debug/process/{processType}/{executionId}
     */
    public String toDebugLocation(String processType, UUID executionId) {
        String s3Delimiter = "/";
        return String.join(s3Delimiter, s3RootPath, "process", processType, executionId.toString());
    }
}
