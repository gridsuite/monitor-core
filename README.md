# Monitor core

[![Actions Status](https://github.com/gridsuite/monitor-core/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/gridsuite/monitor-core/actions)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=org.gridsuite%3Amonitor-core&metric=coverage)](https://sonarcloud.io/component_measures?id=org.gridsuite%monitor-core&metric=coverage)
[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)

`monitor-core` contains the backend services used to configure, launch, and monitor asynchronous GridSuite processes.

## Module Documentation

Read the child module documentation for service-specific details:

- [`monitor-commons`](monitor-commons/README.md): shared contracts used by the server and workers.
- [`monitor-server`](monitor-server/README.md): main REST backend and BFF for `gridmonitor-app`.
- [`monitor-worker-server`](monitor-worker-server/README.md): worker service dedicated to asynchronous computations.

## Overview

`monitor-server` manages process configurations, starts executions, stores execution and step statuses in PostgreSQL, and publishes run requests to RabbitMQ.

`monitor-worker-server` consumes those requests, executes the configured process steps, sends status updates back to the server, and stores computation results or debug artifacts when needed.

## Liquibase

Please read [liquibase usage](https://github.com/powsybl/powsybl-parent/#liquibase-usage) for instructions to automatically generate changesets.
After generating a changeset, add it to Git and reference it in `monitor-server/src/main/resources/db/changelog/db.changelog-master.yaml`.
