/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.result;

import org.gridsuite.monitor.server.types.SecurityAnalysisResultType;
import org.gridsuite.monitor.server.types.ShortCircuitResultType;
import org.springframework.data.domain.Pageable;

/**
 * @author Radouane Khouadri <redouane.khouadri_externe at rte-france.com>
 */
public record ResultQueryParams(
    String resultType,
    Pageable pageable,
    String filters
) {
    public SecurityAnalysisResultType securityAnalysisResultType() {
        return SecurityAnalysisResultType.valueOf(resultType);
    }

    public ShortCircuitResultType shortCircuitResultType() {
        return ShortCircuitResultType.valueOf(resultType);
    }
}
