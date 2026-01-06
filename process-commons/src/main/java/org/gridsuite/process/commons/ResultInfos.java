package org.gridsuite.process.commons;

import java.util.UUID;

public record ResultInfos(
    UUID resultUUID,
    ResultType resultType
) { }
