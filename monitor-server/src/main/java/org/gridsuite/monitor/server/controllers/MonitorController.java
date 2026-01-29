/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gridsuite.monitor.commons.ResultInfos;
import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.server.dto.Report;
import org.gridsuite.monitor.server.services.MonitorService;
import org.gridsuite.monitor.server.services.ReportService;
import org.gridsuite.monitor.server.services.ResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@RestController
@RequestMapping(value = "/" + MonitorApi.API_VERSION + "/")
@Tag(name = "Monitor server")
public class MonitorController {

    private final MonitorService monitorService;
    private final ResultService resultService;
    private final ReportService reportService;

    public MonitorController(MonitorService monitorService, ResultService resultService, ReportService reportService) {
        this.monitorService = monitorService;
        this.resultService = resultService;
        this.reportService = reportService;
    }

    @PostMapping("/execute/security-analysis")
    @Operation(summary = "Execute a security analysis process")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The security analysis execution has been started")})
    public ResponseEntity<UUID> executeSecurityAnalysis(
            @RequestParam UUID caseUuid,
            @RequestBody SecurityAnalysisConfig securityAnalysisConfig) {
        UUID executionId = monitorService.executeProcess(caseUuid, securityAnalysisConfig);
        return ResponseEntity.ok(executionId);
    }

    @GetMapping("/executions/{executionId}/reports")
    @Operation(summary = "Get reports for an execution")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The execution reports")})
    public ResponseEntity<List<Report>> getExecutionReports(@Parameter(description = "Execution UUID") @PathVariable UUID executionId) {
        List<UUID> reportIds = monitorService.getReportIds(executionId);
        List<Report> reports = reportIds.stream()
                    .map(reportService::getReport)
                    .toList();

        return ResponseEntity.ok(reports);
    }

    @GetMapping("/executions/{executionId}/results")
    @Operation(summary = "Get results for an execution")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The execution results")})
    public ResponseEntity<List<String>> getExecutionResults(@Parameter(description = "Execution UUID") @PathVariable UUID executionId) {
        List<ResultInfos> resultInfos = monitorService.getResultInfos(executionId);
        List<String> results = resultInfos.stream()
                .map(resultService::getResult)
                .toList();

        return ResponseEntity.ok(results);
    }

    @DeleteMapping("/executions/{executionId}")
    @Operation(summary = "Delete an execution")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Execution was deleted"),
                           @ApiResponse(responseCode = "404", description = "Execution was not found")})
    public ResponseEntity<Void> deleteExecution(@PathVariable UUID executionId) {
        return monitorService.deleteExecution(executionId) ?
            ResponseEntity.ok().build() :
            ResponseEntity.notFound().build();
    }
}
