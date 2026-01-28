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
import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.server.services.ProcessConfigService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

@RestController
@RequestMapping(value = "/" + MonitorApi.API_VERSION + "/process-configs")
@Tag(name = "Process config")
public class ProcessConfigController {

    private final ProcessConfigService processConfigService;

    public ProcessConfigController(ProcessConfigService processConfigService) {
        this.processConfigService = processConfigService;
    }

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create process config")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "process config was created")})
    public ResponseEntity<UUID> createProcessConfig(
            @RequestBody(required = false) ProcessConfig processConfig) {
        return ResponseEntity.ok().body(processConfigService.createProcessConfig(processConfig));
    }

    @GetMapping(value = "/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get process config")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "process config was returned"),
        @ApiResponse(responseCode = "404", description = "process config was not found")})
    public ResponseEntity<ProcessConfig> getProcessConfig(
            @Parameter(description = "process config UUID") @PathVariable("uuid") UUID processConfigUuid) {
        ProcessConfig processConfig = processConfigService.getProcessConfig(processConfigUuid);
        return processConfig != null ? ResponseEntity.ok().body(processConfig)
                : ResponseEntity.notFound().build();
    }

    @PutMapping(value = "/{uuid}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update process config")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "process config was updated"),
        @ApiResponse(responseCode = "404", description = "process config was not found")})
    public ResponseEntity<Void> updateProcessConfig(
            @Parameter(description = "process config UUID") @PathVariable("uuid") UUID processConfigUuid,
            @RequestBody(required = false) ProcessConfig processConfig) {
        return processConfigService.updateProcessConfig(processConfigUuid, processConfig) ?
            ResponseEntity.ok().build() :
            ResponseEntity.notFound().build();
    }

    @DeleteMapping(value = "/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete process config")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "process config was deleted"),
        @ApiResponse(responseCode = "404", description = "process config was not found")})
    public ResponseEntity<Void> deleteProcessConfig(
            @Parameter(description = "process config UUID") @PathVariable("uuid") UUID processConfigUuid) {
        return processConfigService.deleteProcessConfig(processConfigUuid) ?
            ResponseEntity.ok().build() :
            ResponseEntity.notFound().build();
    }
}
