/*
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
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.dto.processconfig.AutomaticProcessConfigInfos;
import org.gridsuite.monitor.server.services.automaticprocessconfig.AutomaticProcessConfigService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Radouane Khouadri <redouane.khouadri_externe at rte-france.com>
 */
@RestController
@RequestMapping("/" + MonitorApi.API_VERSION + "/automatic-process-configs")
@Tag(name = "Automatic process config")
public class AutomaticProcessConfigController {
    private final AutomaticProcessConfigService automaticProcessConfigService;

    public AutomaticProcessConfigController(AutomaticProcessConfigService automaticProcessConfigService) {
        this.automaticProcessConfigService = automaticProcessConfigService;
    }

    @PutMapping(path = "/{processType}", consumes = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Set the process config used by automatic executions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Automatic process config was updated"),
        @ApiResponse(responseCode = "400", description = "Process config type does not match process type"),
        @ApiResponse(responseCode = "404", description = "Process config was not found")})
    public ResponseEntity<Void> updateAutomaticProcessConfig(
            @PathVariable @Parameter(description = "process type") ProcessType processType,
            @RequestBody String configUuid,
            @RequestHeader(MonitorApi.HEADER_USER_ID) String userId
    ) {
        Optional<UUID> updated = automaticProcessConfigService.update(UUID.fromString(configUuid), processType, userId);
        return updated.isPresent() ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get the process configs currently used by automatic executions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Current automatic process configs")})
    public ResponseEntity<List<AutomaticProcessConfigInfos>> getAutomaticProcessConfigs() {
        return ResponseEntity.ok(automaticProcessConfigService.getActives());
    }

    @GetMapping(path = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get the history of automatic process configs changes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Automatic process configs history")})
    public ResponseEntity<List<AutomaticProcessConfigInfos>> getAutomaticProcessConfigsHistory() {
        return ResponseEntity.ok(automaticProcessConfigService.getHistory());
    }
}
