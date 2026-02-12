/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.dto;

import lombok.Builder;
import lombok.Getter;

import java.io.InputStream;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
@Builder
@Getter
public class S3InputStreamInfos {
    InputStream inputStream;
    String fileName;
    Long fileLength;
}
