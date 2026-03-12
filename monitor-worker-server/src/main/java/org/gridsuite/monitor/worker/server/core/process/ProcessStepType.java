/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.core.process;

/**
 * Functional classification of a {@link ProcessStep}.
 * <p>
 * The returned name is used for display/monitoring and should be stable over time (it may be persisted or consumed
 * by external systems).
 * <p>
 * Typical implementations are enums (e.g. {@code SecurityAnalysisStepType}) providing a concise, unique name per step kind.
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public interface ProcessStepType {

    /**
     * Stable, human-readable identifier of the step type.
     *
     * @return the step type name
     */
    String getName();
}
