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
import jakarta.validation.Valid;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.dto.automaticprocessconfig.AutomaticProcessConfigRequest;
import org.gridsuite.monitor.server.services.automaticprocessconfig.AutomaticProcessConfigService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Radouane Khouadri <redouane.khouadri_externe at rte-france.com>
 */
@RestController
@RequestMapping("/" + MonitorApi.API_VERSION + "/processes/automatic-process-configs")
@Tag(name = "Automatic execution")
public class AutomaticProcessConfigController {
    private final AutomaticProcessConfigService configService;

    public AutomaticProcessConfigController(AutomaticProcessConfigService configService) {
        this.configService = configService;
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Set the process config used by automatic executions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Automatic process config was updated"),
        @ApiResponse(responseCode = "400", description = "Process config type does not match process type"),
        @ApiResponse(responseCode = "404", description = "Process config was not found")})
    public ResponseEntity<Void> updateAutomaticProcessConfig(
        @Valid @RequestBody AutomaticProcessConfigRequest request,
        @RequestHeader(MonitorController.HEADER_USER_ID) String userId
    ) {
        Optional<UUID> updated = configService.update(request, userId);
        return updated.isPresent() ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get the process config used by automatic executions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Automatic process config"),
        @ApiResponse(responseCode = "404", description = "Automatic process config was not found")})
    public ResponseEntity<UUID> getAutomaticProcessConfig(
        @Parameter(description = "Process type") @RequestParam(name = "processType") ProcessType processType
    ) {
        return configService.get(processType)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
