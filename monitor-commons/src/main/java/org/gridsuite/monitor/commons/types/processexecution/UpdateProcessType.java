package org.gridsuite.monitor.commons.types.processexecution;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author Khouloud Kessemtini {@literal <khouloud.kessemtini at rte-france.com>}
 */

@Schema(enumAsRef = true)
public enum UpdateProcessType {
    PROCESS_EXECUTION_UPDATED,
    PROCESS_EXECUTION_STEPS_UPDATED
}
