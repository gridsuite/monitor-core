/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.report;

import com.google.auto.service.AutoService;
import com.powsybl.commons.report.ReportResourceBundle;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@AutoService(ReportResourceBundle.class)
public final class ProcessWorkerServerReportResourceBundle implements ReportResourceBundle {

    public static final String BASE_NAME = "org.gridsuite.process.worker.server.reports";

    public String getBaseName() {
        return BASE_NAME;
    }
}
