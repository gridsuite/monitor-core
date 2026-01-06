package org.gridsuite.process.orchestrator.server.services;

import org.gridsuite.process.commons.ResultType;

import java.util.UUID;

public interface ResultProvider {
    ResultType getType();

    String getResult(UUID resultId);
}
