/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.LineContingency;
import com.powsybl.security.SecurityAnalysisParameters;
import org.gridsuite.modification.dto.AttributeModification;
import org.gridsuite.modification.dto.LoadModificationInfos;
import org.gridsuite.modification.dto.ModificationInfos;
import org.gridsuite.modification.dto.OperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Radouane KHOUADRI {@literal <redouane.khouadri_externe at rte-france.com>}
 */
public class ConfigCompareTest {

    record SecurityAnalysisWrapper(SecurityAnalysisParameters securityAnalysisParameters,
                                   List<Contingency> contingencies,
                                   List<ModificationInfos> modificationInfos) {
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            .registerModule(new Jdk8Module());

    private SecurityAnalysisWrapper w1;
    private SecurityAnalysisWrapper w2;

    @BeforeEach
    public void init() {
        Contingency c1 = new Contingency("NHV1_NHV2_1", "NHV1_NHV2_1", List.of(new LineContingency("NHV1_NHV2_1")));
        Contingency c2 = new Contingency("NHV1_NHV2_2", "NHV1_NHV2_2", List.of(new LineContingency("NHV1_NHV2_2")));
        List<ModificationInfos> modificationInfos = List.of(LoadModificationInfos.builder().equipmentId("load1").q0(new AttributeModification<>(300., OperationType.SET)).build());

        Contingency c22 = new Contingency("NHV1_NHV2_2", "NHV1_NHV2_2", List.of(new LineContingency("NHV1_NHV2_2"),new LineContingency("NHV1_NHV2_2.5")));
        Contingency c3 = new Contingency("NHV1_NHV2_3", "NHV1_NHV2_3", List.of(new LineContingency("NHV1_NHV2_3")));
        List<ModificationInfos> modificationInfos2 = List.of(LoadModificationInfos.builder().equipmentId("load2").q0(new AttributeModification<>(700., OperationType.SET)).build());


        w1 = new SecurityAnalysisWrapper(new SecurityAnalysisParameters(), List.of(c1,c2), modificationInfos);
        w2 = new SecurityAnalysisWrapper(new SecurityAnalysisParameters(), List.of(c22,c3), modificationInfos2);
    }

    @Test
    void diffTest() throws Exception {
        //manual diff
        String html = ManualCompare.compare(w1, w2);
        Files.writeString(Path.of("manual-diff.html"), html);
    }

    @Test
    void diffUtilsTest() throws Exception{
        String html = new DiffUtilsCompare(OBJECT_MAPPER).compare(w1, w2);
        Files.writeString(Path.of("diff-utils.html"), html);

        String html2 = new DiffUtilsCompare(OBJECT_MAPPER).compareSideBySide(w1, w2);
        Files.writeString(Path.of("diff-utils-sides.html"), html2);
    }

    @Test
    void jsondiffTest() throws Exception {
        String patch = JsonDiffCompare.compare(OBJECT_MAPPER, w1, w2);
        System.out.println(patch);
    }

}
