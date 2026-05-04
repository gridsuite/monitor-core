/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
class PropertyServerNameProviderTest {

    @Test
    void returnsProvidedName() {
        PropertyServerNameProvider provider = new PropertyServerNameProvider("monitor-server");
        assertThat(provider.serverName()).isEqualTo("monitor-server");
    }
}
