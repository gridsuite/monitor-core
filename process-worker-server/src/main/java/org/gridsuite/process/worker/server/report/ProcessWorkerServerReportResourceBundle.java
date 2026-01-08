package org.gridsuite.process.worker.server.report;

import com.google.auto.service.AutoService;
import com.powsybl.commons.report.ReportResourceBundle;

@AutoService(ReportResourceBundle.class)
public final class ProcessWorkerServerReportResourceBundle implements ReportResourceBundle {

    public static final String BASE_NAME = "org.gridsuite.process.worker.server.reports";

    public String getBaseName() {
        return BASE_NAME;
    }
}
