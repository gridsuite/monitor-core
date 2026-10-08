/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, software distributed under the terms of the Mozilla Public
 * License is distributed on an "AS IS" BASIS.
 */
package org.gridsuite.monitor.server.dto.processexecution;

import java.util.List;
import java.util.Map;

public record CsvExportRequest(
    List<String> headers,
    Map<String, String> enumValueTranslations,
    String language,
    Boolean oneBusCase
) { }
