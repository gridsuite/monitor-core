/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;
import com.powsybl.shortcircuit.BusFault;
import com.powsybl.shortcircuit.Fault;
import com.powsybl.shortcircuit.ShortCircuitAnalysisProvider;
import com.powsybl.shortcircuit.ShortCircuitParameters;
import org.apache.commons.lang3.StringUtils;
import org.gridsuite.monitor.worker.server.dto.parameters.shortcircuit.ShortCircuitParametersInfos;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@Service
public class ShortCircuitParametersService {
    public static final String NODE_CLUSTER = "nodeCluster";

    public ShortCircuitParameters buildParameters(ShortCircuitParametersInfos parametersInfos, String provider, Map<String, String> specificParameters) {
        ShortCircuitParameters parameters = parametersInfos.getCommonParameters() != null
                ? parametersInfos.getCommonParameters() : ShortCircuitParameters.load();
        parameters.setWithFortescueResult(false);
        parameters.setDetailedReport(false);

        if (specificParameters != null && !specificParameters.isEmpty()) {
            Map<String, String> solverParameters = new HashMap<>(specificParameters);
            if (!solverParameters.isEmpty()) {
                ShortCircuitAnalysisProvider analysisProvider = ShortCircuitAnalysisProvider.findAll().stream()
                        .filter(p -> p.getName().equals(provider))
                        .findFirst().orElseThrow(() -> new PowsyblException("Short circuit provider not found " + provider));
                Extension<ShortCircuitParameters> extension = analysisProvider.loadSpecificParameters(PlatformConfig.defaultConfig())
                        .orElseThrow(() -> new PowsyblException("Cannot load specific short circuit parameters for provider " + provider));
                parameters.addExtension((Class) extension.getClass(), extension);
                analysisProvider.updateSpecificParameters(extension, solverParameters);
            }
        }
        return parameters;
    }

    private List<String> deserializeNodeClusters(Map<String, String> specificParameters) {
        String rawNodeClusters = specificParameters.get(NODE_CLUSTER);
        if (Objects.equals(rawNodeClusters, "") || rawNodeClusters == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(rawNodeClusters.split(", ")).map(String::trim).toList();
    }

    public List<Fault> getAllBusFaults(Network network, Map<String, String> specificParameters) {
        Stream<Bus> busesStream = network.getBusView().getBusStream();
        // If there is a configured ZI, then only BusFault for this ZI are returned, it returns all the network otherwise
        if (specificParameters != null && specificParameters.containsKey(NODE_CLUSTER)) {
            List<String> nodeClusters = deserializeNodeClusters(specificParameters);
            if (!nodeClusters.isEmpty()) {
                busesStream = busesStream.filter(bus -> nodeClusters.contains(bus.getId()));
            }
        }
        return busesStream.map(bus -> new BusFault(bus.getId(), bus.getId())).collect(Collectors.toList());
    }

    public void checkInconsistentVoltageLevels(Network network, ReportNode reportNode) {
        List<String> inconsistentVoltageLevels = new ArrayList<>();
        network.getVoltageLevelStream().forEach(vl -> {
            IdentifiableShortCircuit<VoltageLevel> shortCircuitExtension = vl.getExtension(IdentifiableShortCircuit.class);
            if (shortCircuitExtension != null && shortCircuitExtension.getIpMin() > shortCircuitExtension.getIpMax()) {
                inconsistentVoltageLevels.add(vl.getId());
            }
        });
        if (!inconsistentVoltageLevels.isEmpty()) {
            reportNode.newReportNode()
                .withMessageTemplate("monitor.worker.server.shortcircuit.step.error.VoltageLevelsWithWrongIscValuesSummarize")
                .withUntypedValue("voltageLevels", StringUtils.join(inconsistentVoltageLevels, ", "))
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .withTimestamp()
                .add();
            throw new RuntimeException("Some voltage levels have wrong isc values. Check out the logs to find which ones");
        }
    }
}
