# Monitor server

## Presentation

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

## Technologies

- Spring Boot
- Spring Data JPA / Hibernate
- Spring Cloud Stream / RabbitMQ
- PostgreSQL
- Liquibase
- AWS SDK S3

## Status Management

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
