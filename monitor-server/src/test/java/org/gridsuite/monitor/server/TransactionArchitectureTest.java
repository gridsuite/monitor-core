/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
    packages = "org.gridsuite.monitor.server",
    importOptions = ImportOption.DoNotIncludeTests.class
)
final class TransactionArchitectureTest {

    private TransactionArchitectureTest() {
    }

    @ArchTest
    static final ArchRule TRANSACTIONS_DO_NOT_USE_EXTERNAL_IO = noClasses()
        .that().haveSimpleNameEndingWith("TxService")
        .should().dependOnClassesThat().resideInAnyPackage(
            "org.gridsuite.monitor.server.clients..",
            "org.gridsuite.monitor.server.messaging..",
            "org.gridsuite.monitor.server.services.result.."
        )
        .because("external I/O must happen outside database transactions");

    @ArchTest
    static final ArchRule TRANSACTIONS_DO_NOT_CALL_ORCHESTRATION = noClasses()
        .that().haveSimpleNameEndingWith("TxService")
        .should().dependOnClassesThat().haveNameMatching(
            ".*\\.(ProcessExecutionService|ProcessConfigService)"
        )
        .because("transactional services must not enter orchestration workflows");

    @ArchTest
    static final ArchRule ORCHESTRATION_DOES_NOT_ACCESS_REPOSITORIES = noClasses()
        .that().haveNameMatching(
            ".*\\.(ProcessExecutionService|ProcessConfigService)"
        )
        .should().dependOnClassesThat().resideInAnyPackage(
            "org.gridsuite.monitor.server.repositories.."
        )
        .because("database access belongs in transactional services");
}
