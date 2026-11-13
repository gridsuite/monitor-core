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
import org.gridsuite.monitor.server.services.automaticprocessconfig.AutomaticProcessConfigTxService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Radouane Khouadri <redouane.khouadri_externe at rte-france.com>
 */
@RestController
@RequestMapping("/" + MonitorApi.API_VERSION + "/automatic-process-configs")
@Tag(name = "Automatic process config")
public class AutomaticProcessConfigController {
    private final AutomaticProcessConfigTxService configService;

    public AutomaticProcessConfigController(AutomaticProcessConfigTxService configService) {
        this.configService = configService;
    }

    @PutMapping(path = "/{processType}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Set the process config used by automatic executions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Automatic process config was updated"),
        @ApiResponse(responseCode = "400", description = "Process config type does not match process type"),
        @ApiResponse(responseCode = "404", description = "Process config was not found")})
    @PreAuthorize("@authorizationService.canWrite(#configUuid)")
    public ResponseEntity<Void> updateAutomaticProcessConfig(
            @PathVariable @Parameter(description = "process type") ProcessType processType,
            @RequestBody UUID configUuid) {
        Optional<UUID> updated = configService.update(configUuid, processType, SecurityContextHolder.getContext().getAuthentication().getName());
        return updated.isPresent() ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get the process config used by automatic executions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Automatic process config"),
        @ApiResponse(responseCode = "404", description = "Automatic process config was not found")})
    @PreAuthorize("true")
    public ResponseEntity<UUID> getAutomaticProcessConfig(
        @Parameter(description = "Process type") @RequestParam(name = "processType") ProcessType processType
    ) {
        return configService.get(processType)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
