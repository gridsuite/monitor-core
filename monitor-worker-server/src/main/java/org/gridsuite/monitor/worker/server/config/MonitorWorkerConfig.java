/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.config;

import com.fasterxml.jackson.databind.InjectableValues;
import com.powsybl.commons.report.ReportNodeDeserializer;
import com.powsybl.commons.report.ReportNodeJsonModule;
import com.powsybl.security.json.SecurityAnalysisJsonModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Configuration
public class MonitorWorkerConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.postConfigurer(objectMapper -> {
            objectMapper.registerModule(new SecurityAnalysisJsonModule());
            objectMapper.registerModule(new ReportNodeJsonModule());
            objectMapper.setInjectableValues(new InjectableValues.Std()
                .addValue(ReportNodeDeserializer.DICTIONARY_VALUE_ID, null));
        });
    }
}
