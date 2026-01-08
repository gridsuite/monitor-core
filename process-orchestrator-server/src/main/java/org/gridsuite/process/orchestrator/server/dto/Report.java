package org.gridsuite.process.orchestrator.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Schema(description = "Report data")
@Builder
public record Report(
        UUID id,
        UUID parentId,
        String message,
        Severity severity,
        List<Report> subReports
) { }
