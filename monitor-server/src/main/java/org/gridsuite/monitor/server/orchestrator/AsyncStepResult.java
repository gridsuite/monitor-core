/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.orchestrator;

import org.gridsuite.monitor.commons.types.result.ResultType;

import java.util.UUID;

/**
 * Data received when an async step completes via RabbitMQ callback.
 * Contains our echoed metadata plus the computation result reference.
 *
 * @param executionId        the process execution this step belongs to
 * @param completedStepIndex the index of the step that just completed
 * @param caseS3Key          the S3 key of the case (not persisted in DB, travels in the message)
 * @param resultUuid         the UUID of the computation result
 * @param resultType         the type of result produced
 * @param success            whether the async computation succeeded
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public record AsyncStepResult(
    UUID executionId,
    int completedStepIndex,
    String caseS3Key,
    UUID resultUuid,
    ResultType resultType,
    boolean success
) {
}
