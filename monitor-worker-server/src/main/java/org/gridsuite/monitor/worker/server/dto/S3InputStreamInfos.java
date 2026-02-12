package org.gridsuite.monitor.worker.server.dto;

import lombok.Builder;
import lombok.Getter;

import java.io.InputStream;

@Builder
@Getter
public class S3InputStreamInfos {
    InputStream inputStream;
    String fileName;
    Long fileLength;
}
