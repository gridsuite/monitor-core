package org.gridsuite.process.commons;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "processType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = SecurityAnalysisConfig.class, name = "SECURITY_ANALYSIS")
})
public interface ProcessConfig {
    ProcessType processType();

    UUID caseUuid();

    UUID executionId();

    ProcessConfig withExecutionId(UUID executionId);
}
