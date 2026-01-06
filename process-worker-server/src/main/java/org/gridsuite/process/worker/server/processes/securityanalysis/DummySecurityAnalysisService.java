/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.process.worker.server.processes.securityanalysis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.process.commons.ResultInfos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class DummySecurityAnalysisService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DummySecurityAnalysisService.class);
    private final ObjectMapper objectMapper;

    public DummySecurityAnalysisService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void saveResult(ResultInfos resultInfos, Object result) {
        try {
            String resultJson = objectMapper.writeValueAsString(result);
            LOGGER.info("saved results uuid : {} content : {}...", resultInfos.resultUUID(), resultJson.substring(0, Math.min(resultJson.length(), 500)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
