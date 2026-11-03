# Monitor server

## Description

Main backend exposing the REST API.

BFF for `gridmonitor-app`.

Responsibilities:

- manage process configurations with CRUD operations;
- launch executions asynchronously;
- track execution and step statuses;
- persist data in PostgreSQL;
- expose results, reports, and debug files;
- publish tasks to RabbitMQ;
- coordinate with workers;
- handle cross-cutting concerns such as user tracking, timestamps, errors, and execution states.

## Technical Stack

- Spring Boot (Web, Data JPA, Actuator)
- Spring Cloud Stream with RabbitMQ
- PostgreSQL
- Liquibase
- Micrometer / Prometheus
- AWS SDK S3 (Spring Cloud AWS)
- MapStruct / Lombok


## Development Scripts

Build Docker image

```shell
mvn install -DskipTests -Dpowsybl.docker.install
```

Please read [liquibase usage](https://github.com/powsybl/powsybl-parent/#liquibase-usage) for instructions to automatically generate changesets. After you generated a changeset do not forget to add it to git and in src/resource/db/changelog/db.changelog-master.yml

## Transaction Management

Transactions are isolated in dedicated Spring beans such as `ProcessExecutionTxService` and `ProcessConfigTxService`.
The regular services orchestrate validation, database operations, REST/S3 calls, and messaging without wrapping the whole workflow in `@Transactional`.

- Put database operations in `*TxService` methods.
- Keep external I/O outside these methods so network latency does not hold database connections or locks, and remote failures do not unnecessarily roll back database work.
- Call the injected `*TxService` bean rather than a transactional method on `this`: Spring's transaction proxy does not intercept self-invocation.

For example, `ProcessExecutionService.executeProcess` calls `ProcessExecutionTxService.createExecution` before publishing messages.
When called without an outer transaction, the database commit completes before publication, so workers can read the execution.
This does not make database changes and external operations atomic: a later REST or messaging failure cannot undo an already committed transaction and may require retry or compensation.


# Status Management

`monitor-server` stores the status of process executions and process steps.

`monitor-worker-server` orchestrates step sequencing and status transitions.

Execution statuses:

- `SCHEDULED`
- `RUNNING`
- `COMPLETED`
- `FAILED`

Step statuses:

- `SCHEDULED`
- `RUNNING`
- `COMPLETED`
- `FAILED`
- `SKIPPED`

## Error Management

Errors are handled with the existing GridSuite mechanism based on `AbstractBusinessExceptionHandler`.
