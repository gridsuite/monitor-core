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
import org.gridsuite.monitor.commons.ProcessExecutionStep;
import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.server.dto.Report;
import org.gridsuite.monitor.server.services.MonitorService;
import org.springframework.http.ResponseEntity;
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

    public MonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
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
        List<Report> reports = monitorService.getReports(executionId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/executions/{executionId}/results")
    @Operation(summary = "Get results for an execution")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The execution results")})
    public ResponseEntity<List<String>> getExecutionResults(@Parameter(description = "Execution UUID") @PathVariable UUID executionId) {
        List<String> results = monitorService.getResults(executionId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/executions/{executionId}/step-infos")
    @Operation(summary = "Get execution steps statuses")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The execution steps statuses")})
    public ResponseEntity<List<ProcessExecutionStep>> getStepsInfos(@Parameter(description = "Execution UUID") @PathVariable UUID executionId) {
        return ResponseEntity.ok(monitorService.getStepsInfos(executionId));
    }
}
