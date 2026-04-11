package org.gridsuite.monitor.server.testdata.processconfig;

import java.util.List;

public final class ProcessConfigProviders {

    private ProcessConfigProviders() {
    }

    private static List<ProcessConfigProvider<?, ?>> providers;

    public static List<ProcessConfigProvider<?, ?>> all() {
        if (providers == null) {
            providers = List.of(
                    new LoadFlowConfigProvider(),
                    new SecurityAnalysisConfigProvider());
        }

        return providers;
    }
}
