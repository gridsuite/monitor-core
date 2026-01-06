package org.gridsuite.process.orchestrator.server.services;

import org.gridsuite.process.commons.ResultType;
import org.gridsuite.process.commons.ResultInfos;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ResultService {

    private final Map<ResultType, ResultProvider> providers;

    public ResultService(List<ResultProvider> resultProviders) {
        this.providers = resultProviders.stream()
                .collect(Collectors.toUnmodifiableMap(
                        ResultProvider::getType,
                        Function.identity()
                ));
    }

    public String getResult(ResultInfos resultInfos) {
        ResultProvider provider = providers.get(resultInfos.resultType());
        if (provider != null) {
            return provider.getResult(resultInfos.resultUUID());
        } else {
            throw new IllegalArgumentException("Unsupported result type: " + resultInfos.resultType());
        }
    }
}
