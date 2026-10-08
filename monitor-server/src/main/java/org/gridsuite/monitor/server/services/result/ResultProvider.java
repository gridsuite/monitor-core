/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.result;

import org.gridsuite.monitor.commons.types.result.ResultType;

import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public interface ResultProvider {
    ResultType getType();

    default String getResult(UUID resultId) {
        return getResult(resultId, null);
    }

    String getResult(UUID resultId, ResultQueryParams queryParams);

    default byte[] exportResult(UUID resultId, ResultQueryParams queryParams, String csvTranslations) {
        throw new UnsupportedOperationException("CSV export is not supported for " + getType());
    }

    void deleteResult(UUID resultId);
}
