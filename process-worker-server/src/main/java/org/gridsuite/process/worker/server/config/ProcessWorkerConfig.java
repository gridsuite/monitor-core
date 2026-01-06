package org.gridsuite.process.worker.server.config;

import com.fasterxml.jackson.databind.InjectableValues;
import com.powsybl.commons.report.ReportNodeDeserializer;
import com.powsybl.commons.report.ReportNodeJsonModule;
import com.powsybl.security.json.SecurityAnalysisJsonModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ProcessWorkerConfig {

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