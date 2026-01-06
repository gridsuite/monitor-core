package org.gridsuite.process.worker.server.dto;

import com.powsybl.commons.report.ReportNode;

import java.util.UUID;

public record ReportInfos(UUID reportUuid, ReportNode reportNode) { }
