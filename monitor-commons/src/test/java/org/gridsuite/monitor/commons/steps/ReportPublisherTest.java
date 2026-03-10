/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons.steps;

import com.powsybl.commons.report.ReportNode;
import org.gridsuite.monitor.commons.types.report.ReportInfos;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Mohamed Ben-rejeb <mohamed.ben-rejeb at rte-france.com>
 */
class ReportPublisherTest {

    @Test
    void sendReportShouldForwardReportInfosToImplementation() {
        AtomicReference<ReportInfos> published = new AtomicReference<>();
        ReportPublisher reportPublisher = published::set;
        ReportInfos reportInfos = new ReportInfos(UUID.randomUUID(), ReportNode.NO_OP);

        reportPublisher.sendReport(reportInfos);

        assertSame(reportInfos, published.get());
    }
}
