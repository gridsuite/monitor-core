/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.core.exceptions;

/**
 * @author Radouane KHOUADRI {@literal <redouane.khouadri_externe at rte-france.com>}
 */
public class ProcessCancelledException extends RuntimeException {
    public ProcessCancelledException(String message) {
        super(message);
    }

    public ProcessCancelledException(String message, Throwable cause) {
        super(message, cause);
    }
}
