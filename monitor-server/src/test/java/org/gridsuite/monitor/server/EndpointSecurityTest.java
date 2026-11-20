/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.monitor.server;

import com.powsybl.ws.commons.security.restclient.AbstractEndpointSecurityTest;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author Caroline Jeandat <caroline.jeandat at rte-france.com>
 */
@SpringBootTest
class EndpointSecurityTest extends AbstractEndpointSecurityTest {

    protected List<String> getControllerPackages() {
        return List.of("org.gridsuite.monitor.server.controllers");
    }

    /**
     * Supervision endpoints are filtered by the gateway and are not directly accessible,
     * so we don't need to check permissions for them.
     */
    protected List<Class<?>> getExcludedControllers() {
        return List.of();
    }
}
