/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.utils;

import com.powsybl.commons.report.ReportNode;
import org.apache.commons.text.StringSubstitutor;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class ReportTestUtils {
    private ReportTestUtils() {
        throw new IllegalCallerException("Utility class");
    }

    public static boolean assertMessageFoundFromReporter(String expectedMessage, String reportKey, ReportNode reporterModel) {
        StringSubstitutor reporterSubstitutor = new StringSubstitutor(reporterModel.getValues());
        return containsMessage(expectedMessage, reportKey, reporterModel, reporterSubstitutor);
    }

    private static boolean containsMessage(String expectedMessage, String reportKey, ReportNode reporter, StringSubstitutor reporterSubstitutor) {
        for (ReportNode child : reporter.getChildren()) {
            if (reportKey.equals(child.getMessageKey()) && expectedMessage.equals(formatReportMessage(child, reporterSubstitutor))) {
                return true;
            }
            StringSubstitutor childSubstitutor = new StringSubstitutor(child.getValues());
            if (containsMessage(expectedMessage, reportKey, child, childSubstitutor)) {
                return true;
            }
        }
        return false;
    }

    private static String formatReportMessage(ReportNode report, StringSubstitutor reporterSubstitutor) {
        String reportMessage = new StringSubstitutor(report.getValues()).replace(report.getMessageTemplate());
        return reporterSubstitutor.replace(reportMessage).trim();
    }
}

