/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.processes.commons.steps;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import org.apache.commons.collections4.CollectionUtils;
import org.gridsuite.modification.dto.ModificationInfos;
import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.worker.server.core.AbstractProcessStep;
import org.gridsuite.monitor.worker.server.core.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.services.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 *     Apply modifications passed in context to network passed in context<br/>
 *     If <b>debug</b> is enabled, resulting network will be saved into S3
 *
 *     @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Component
public class ApplyModificationsStep<C extends ProcessConfig> extends AbstractProcessStep<C> {

    private final NetworkModificationService networkModificationService;
    private final NetworkModificationRestService networkModificationRestService;
    private final S3Service s3Service;
    private final FilterService filterService;

    private static final String DEBUG_FILENAME_PREFIX = "debug";
    private static final String DEBUG_FILENAME_SUFFIX = ".xiidm";

    public ApplyModificationsStep(NetworkModificationService networkModificationService,
                                  NetworkModificationRestService networkModificationRestService,
                                  S3Service s3Service,
                                  FilterService filterService) {
        super(CommonStepType.APPLY_MODIFICATIONS);
        this.networkModificationService = networkModificationService;
        this.networkModificationRestService = networkModificationRestService;
        this.s3Service = s3Service;
        this.filterService = filterService;
    }

    @Override
    public void execute(ProcessStepExecutionContext<C> context) {
        List<UUID> modificationIds = context.getConfig().modificationUuids();
        Network network = context.getNetwork();
        if (CollectionUtils.isNotEmpty(modificationIds)) {
            applyModifications(modificationIds, network, context.getReportInfos().reportNode());
        }
        if (context.getDebugFileLocation() != null) {
            try {
                exportUpdatedNetworkToS3(context);
            } catch (IOException e) {
                throw new PowsyblException("An error occurred while saving debug file: " + e.getCause(), e);
            }
        }
    }

    private void exportUpdatedNetworkToS3(ProcessStepExecutionContext<C> context) throws IOException {
        s3Service.exportCompressedToS3(
            getDebugFilePath(context, String.join("", DEBUG_FILENAME_PREFIX, DEBUG_FILENAME_SUFFIX, ".gz")),
            DEBUG_FILENAME_PREFIX,
            DEBUG_FILENAME_SUFFIX,
            networkFile -> context.getNetwork().write("XIIDM", null, networkFile)
        );
    }

    private void applyModifications(List<UUID> modificationIds, Network network, ReportNode reportNode) {
        List<ModificationInfos> modificationInfos = networkModificationRestService.getModifications(modificationIds);
        networkModificationService.applyModifications(network, modificationInfos, reportNode, filterService);
    }
}
