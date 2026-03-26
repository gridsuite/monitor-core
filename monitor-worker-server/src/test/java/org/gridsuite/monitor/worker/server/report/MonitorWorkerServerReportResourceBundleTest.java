/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.report;

import com.powsybl.commons.report.ReportResourceBundle;
import org.junit.jupiter.api.Test;

import java.util.ResourceBundle;
import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
class MonitorWorkerServerReportResourceBundleTest {

    @Test
    void reportResourceBundleIsRegisteredViaAutoService() {
        ServiceLoader<ReportResourceBundle> loader = ServiceLoader.load(ReportResourceBundle.class);

        assertThat(loader).anyMatch(MonitorWorkerServerReportResourceBundle.class::isInstance);
    }

    @Test
    void reportBundleIsFound() {
        assertThatNoException().isThrownBy(() ->
            ResourceBundle.getBundle(MonitorWorkerServerReportResourceBundle.BASE_NAME)
        );
    }
}
