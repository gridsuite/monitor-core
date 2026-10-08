package org.gridsuite.monitor.server.services.result;

import org.gridsuite.monitor.commons.types.result.SecurityAnalysisResultType;
import org.gridsuite.monitor.commons.types.result.ShortCircuitResultType;

import java.util.List;

public record ResultQueryParams(
    String resultType,
    Integer page,
    Integer size,
    List<String> sort,
    String filters
) {
    public ResultQueryParams {
        sort = sort == null ? List.of() : List.copyOf(sort);
    }

    public SecurityAnalysisResultType securityAnalysisResultType() {
        return resultType == null ? SecurityAnalysisResultType.NMK_CONTINGENCIES : SecurityAnalysisResultType.valueOf(resultType);
    }

    public ShortCircuitResultType shortCircuitResultType() {
        return resultType == null ? ShortCircuitResultType.ALL_BUSES : ShortCircuitResultType.valueOf(resultType);
    }
}
