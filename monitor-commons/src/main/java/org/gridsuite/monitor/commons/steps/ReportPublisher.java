/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons.steps;

import org.gridsuite.monitor.commons.ReportInfos;

/**
 * @author Mohamed Ben-rejeb <mohamed.ben-rejeb at rte-france.com>
 */
@FunctionalInterface
public interface ReportPublisher {

    void sendReport(ReportInfos reportInfos);
}
