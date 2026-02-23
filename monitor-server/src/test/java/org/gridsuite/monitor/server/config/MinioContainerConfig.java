/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

import java.time.Duration;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
public interface MinioContainerConfig {
    String MINIO_DOCKER_IMAGE_NAME = "minio/minio";
    String BUCKET_NAME = "ws-bucket";
    // Just a fixed version, latest at the time of writing this
    String MINIO_DOCKER_IMAGE_VERSION = "RELEASE.2023-09-27T15-22-50Z";
    int MINIO_PORT = 9000;
    GenericContainer<?> MINIO_CONTAINER = createMinioContainer();
    static GenericContainer<?> createMinioContainer() {
        try {
            GenericContainer<?> minioContainer = new GenericContainer(
                String.format("%s:%s", MINIO_DOCKER_IMAGE_NAME, MINIO_DOCKER_IMAGE_VERSION))
                .withCommand("server /data")
                .withExposedPorts(MINIO_PORT)
                .waitingFor(new HttpWaitStrategy()
                    .forPath("/minio/health/ready")
                    .forPort(MINIO_PORT)
                    .withStartupTimeout(Duration.ofSeconds(10)));
            minioContainer.start();
            minioContainer.execInContainer("mkdir", "/data/" + BUCKET_NAME);
            return minioContainer;
        } catch (Exception e) {
            throw new RuntimeException("Failed to start minioContainer", e);
        }
    }

    @DynamicPropertySource
    static void registerAwsProperties(DynamicPropertyRegistry registry) {
        Integer mappedPort = MINIO_CONTAINER.getFirstMappedPort();
        Testcontainers.exposeHostPorts(mappedPort);
        String minioContainerUrl = String.format("http://172.17.0.1:%s", mappedPort);

        registry.add("spring.cloud.aws.endpoint", () -> minioContainerUrl);
    }
}
