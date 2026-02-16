/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.config;

import org.gridsuite.monitor.server.services.S3RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
@Configuration
public class S3Configuration {
    private static final Logger LOGGER = LoggerFactory.getLogger(S3Configuration.class);
    @Value("${spring.cloud.aws.bucket:ws-bucket}")
    private String bucketName;

    @SuppressWarnings("checkstyle:MethodName")
    @Bean
    public S3RestService s3RestService(S3Client s3Client) {
        LOGGER.info("Configuring S3Service with bucket: {}", bucketName);
        return new S3RestService(s3Client, bucketName);
    }
}
