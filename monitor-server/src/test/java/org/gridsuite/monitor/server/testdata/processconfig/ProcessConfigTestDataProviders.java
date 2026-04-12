package org.gridsuite.monitor.server.testdata.processconfig;

import java.util.List;

public final class ProcessConfigTestDataProviders {

    private ProcessConfigTestDataProviders() {
    }

    private static List<ProcessConfigTestDataProvider<?, ?>> providers;

    public static List<ProcessConfigTestDataProvider<?, ?>> all() {
        if (providers == null) {
            providers = List.of(
                    new LoadFlowConfigTestDataProvider(),
                    new SecurityAnalysisConfigTestDataProvider());
        }

        return providers;
    }
}
