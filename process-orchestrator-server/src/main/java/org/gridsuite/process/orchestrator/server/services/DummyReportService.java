package org.gridsuite.process.orchestrator.server.services;

import org.gridsuite.process.orchestrator.server.dto.Report;
import org.gridsuite.process.orchestrator.server.dto.Severity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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
