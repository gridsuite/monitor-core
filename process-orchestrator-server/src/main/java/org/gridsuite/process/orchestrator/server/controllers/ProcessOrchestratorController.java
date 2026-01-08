/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.process.orchestrator.server.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.gridsuite.process.commons.SecurityAnalysisConfig;
import org.gridsuite.process.orchestrator.server.dto.Report;
import org.gridsuite.process.orchestrator.server.services.ProcessOrchestratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@RestController
@RequestMapping(value = "/" + ProcessOrchestratorApi.API_VERSION + "/")
@Tag(name = "process-orchestrator-server")
public class ProcessOrchestratorController {

    private final ProcessOrchestratorService orchestratorService;

    public ProcessOrchestratorController(ProcessOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @PostMapping("/execute/security-analysis")
    public ResponseEntity<UUID> executeSecurityAnalysis(@RequestBody SecurityAnalysisConfig securityAnalysisConfig) {
        UUID executionId = orchestratorService.executeProcess(securityAnalysisConfig);
        return ResponseEntity.ok(executionId);
    }

    @GetMapping("/executions/{executionId}/reports")
    public ResponseEntity<List<Report>> getExecutionReports(@PathVariable UUID executionId) {
        List<Report> reports = orchestratorService.getReports(executionId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/executions/{executionId}/results")
    public ResponseEntity<List<String>> getExecutionResults(@PathVariable UUID executionId) {
        List<String> results = orchestratorService.getResults(executionId);
        return ResponseEntity.ok(results);
    }
}
