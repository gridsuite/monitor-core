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

### Build Docker image

```shell
mvn install -DskipTests -Dpowsybl.docker.install
```

### Generate the OpenAPI specification

The generated file is `monitor-server/target/openapi.json`.

#### Build and start the server

Run from the repository root with the local infrastructure (Postgres, RabbitMQ) running:

```shell
mvn -pl monitor-server -am clean install -DskipTests &&
mvn -pl monitor-server spring-boot:start \
  org.springdoc:springdoc-openapi-maven-plugin:1.5:generate \
  spring-boot:stop \
  -Dspring-boot.run.profiles=local \
  -Dspringdoc.apiDocsUrl=http://localhost:5043/v3/api-docs
```

#### Use an already running server

If the server is already running locally on port 5043, generate the specification without rebuilding or starting/stopping the server:

```shell
mvn -pl monitor-server \
  org.springdoc:springdoc-openapi-maven-plugin:1.5:generate \
  -Dspringdoc.apiDocsUrl=http://localhost:5043/v3/api-docs
```

### Generate Liquibase changesets

Please read [liquibase usage](https://github.com/powsybl/powsybl-parent/#liquibase-usage) for instructions to automatically generate changesets. After you generated a changeset do not forget to add it to git and in src/resource/db/changelog/db.changelog-master.yml

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
