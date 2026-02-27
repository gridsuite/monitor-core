/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.dto.report;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Custom DTO for report logs pagination to ensure JSON serialization compatibility.
 * Spring's Page and Pageable objects contain circular references and can generate
 * non-serializable instances (e.g., Pageable.unpaged()), making them unsuitable
 * for direct API responses. This record contains only the essential pagination
 * data needed by clients.
 */
public record ReportPage(int number, List<ReportLog> content, long totalElements, int totalPages) {
    public ReportPage(Page<ReportLog> page) {
        this(page.getNumber(), page.getContent(), page.getTotalElements(), page.getTotalPages());
    }
}
