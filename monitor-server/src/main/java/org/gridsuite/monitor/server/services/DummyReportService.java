/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import org.gridsuite.monitor.server.dto.Report;
import org.gridsuite.monitor.server.dto.Severity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
public class DummyReportService {
    public Report getReport(UUID reportId) {
        return new Report(
                reportId,
                null,
                "This is a fake report for ID " + reportId,
                Severity.INFO,
                List.of()
        );
    }
}
