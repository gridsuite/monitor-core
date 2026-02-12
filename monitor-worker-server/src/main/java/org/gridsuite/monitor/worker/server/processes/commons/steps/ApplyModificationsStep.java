/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.processes.commons.steps;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import org.apache.commons.collections4.CollectionUtils;
import org.gridsuite.modification.dto.ModificationInfos;
import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.worker.server.core.AbstractProcessStep;
import org.gridsuite.monitor.worker.server.core.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.services.FilterService;
import org.gridsuite.monitor.worker.server.services.NetworkModificationRestService;
import org.gridsuite.monitor.worker.server.services.NetworkModificationService;
import org.gridsuite.monitor.worker.server.services.S3Service;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

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

    private static final String DEBUG_FILENAME = "debug.xiidm.gz";

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
        if (context.isDebug()) {
            try {
                exportUpdatedNetworkToS3(context);
            } catch (IOException e) {
                throw new PowsyblException("An error occurred while saving debug file", e);
            }
        }
    }

    private void exportUpdatedNetworkToS3(ProcessStepExecutionContext<C> context) throws IOException {
        FileAttribute<Set<PosixFilePermission>> tempDirectoryAttributes = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
        Path tempDirectory = Files.createTempDirectory("process-debug", tempDirectoryAttributes);

        Path tempDebugFile = Files.createTempFile(tempDirectory, "debug", ".xiidm");
        Path tempGzFile = Files.createTempFile(tempDirectory, "debug", ".xiidm.gz");
        try {
            DataSource ds = DataSource.fromPath(tempDebugFile);
            context.getNetwork().write("XIIDM", null, ds);
            try (InputStream in = Files.newInputStream(tempDebugFile);
                OutputStream out = new GZIPOutputStream(Files.newOutputStream(tempGzFile))) {
                in.transferTo(out);
            }

            s3Service.uploadFile(tempGzFile, getDebugFilePath(context, DEBUG_FILENAME), DEBUG_FILENAME);
        } finally {
            Files.deleteIfExists(tempDebugFile);
            Files.deleteIfExists(tempGzFile);
            Files.deleteIfExists(tempDirectory);
        }
    }

    private void applyModifications(List<UUID> modificationIds, Network network, ReportNode reportNode) {
        List<ModificationInfos> modificationInfos = networkModificationRestService.getModifications(modificationIds);
        networkModificationService.applyModifications(network, modificationInfos, reportNode, filterService);
    }
}
