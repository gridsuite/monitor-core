package org.gridsuite.process.commons;

import lombok.Getter;

@Getter
public enum ProcessType {
    SECURITY_ANALYSIS("publishRunSa-out-0");

    private final String bindingName;

    ProcessType(String bindingName) {
        this.bindingName = bindingName;
    }

}
