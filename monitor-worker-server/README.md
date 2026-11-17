# Monitor worker server

## Description

Service dedicated to asynchronous computation execution.

It consumes RabbitMQ messages produced by `monitor-server`.

Responsibilities:

- execute PowSyBL computation pipelines, such as Load Flow and Security Analysis;
- chain processing steps;
- update execution statuses;
- produce and store results;
- handle debug mode by exporting intermediate files, such as XIIDM networks, and uploading artifacts to S3;
- support scalability by running multiple worker instances.

## Technical Stack

- Spring Boot (Web, Validation, Actuator)
- Spring Cloud Stream with RabbitMQ
- PowSyBL
- Micrometer / Prometheus
- AWS SDK S3 (Spring Cloud AWS)

## Development Scripts

Build Docker image

```shell
mvn install -DskipTests -Dpowsybl.docker.install
```

## Worker Configuration

`monitor-worker-server` contains the code needed to execute all process types.

There is no separate module by computation type: a single artifact is produced.

The process type handled by a worker is defined by the `worker.process` property in `application.yaml`:

```yaml
worker:
  process: securityanalysis
```

This property controls the RabbitMQ queue consumed by the worker:

```text
monitor.process.${worker.process}.run
```

To deploy several worker types, run several instances of the same artifact with different `worker.process` values.

## Properties

| Property | Default value | Description |
| --- | --- | --- |
| `worker.execution-env-name` | `default-env` | Name of the worker execution environment. |
| `worker.process` | `securityanalysis` | Process type handled by this worker instance. |
| `shortcircuit-analysis.scp-min-provider` | `Courcirc` | Name of the injected SCP-min provider used by the short-circuit step. |
| `gridsuite.services.pccmin-server.base-uri` | `http://pcc-min-server/` | PCC-min server receiving SCP-min results. |

For private RTE deployments, add `gridsuite-scp-min-provider-impl` and its runtime
dependencies to the worker classpath and configure Courcirc. The short-circuit
step calls `ScpMinProvider.find(name)` directly and runs SCP-min through the task
manager with the same selected buses, parameters and report as the short-circuit
calculation. Successful SCP-min results are saved to PCC-min server using
`POST /v1/results/{resultUuid}`, with the same UUID as the short-circuit result
saved to shortcircuit-server. A failed result upload fails the step. There is no
enable flag: the named provider must be available
through JAR injection when the step executes.

## Sequence diagram of a process execution
![Sequence diagram of a process execution](docs/diagrams/sequence_diagram.drawio.svg)

## Error Management

If a step throws an exception:

- the step status switches to `FAILED`;
- an error message is added to the `ReportNode`;
- the report is sent to `report-server`;
- remaining steps switch to `SKIPPED`;
- the execution status switches to `FAILED`;
- the exception is propagated after processing.
