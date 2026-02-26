package com.momo.playback.integration;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

class OssWriteIntegrationTest {

    private static final String DEFAULT_ENDPOINT = "https://oss-us-west-1.aliyuncs.com";
    private static final String TARGET_BUCKET = "mypifi-test";
    private static final String TARGET_PREFIX = "mytest";

    @Test
    void shouldWriteAndReadObjectInMytestPrefix() throws IOException {
        Assumptions.assumeTrue(Boolean.parseBoolean(System.getProperty("runOssWriteTest", "false")),
                "set -DrunOssWriteTest=true to run live OSS write test");

        String accessKeyId = System.getenv("OSS_ACCESS_KEY_ID");
        String accessKeySecret = System.getenv("OSS_ACCESS_KEY_SECRET");
        Assumptions.assumeTrue(hasText(accessKeyId), "OSS_ACCESS_KEY_ID must be set");
        Assumptions.assumeTrue(hasText(accessKeySecret), "OSS_ACCESS_KEY_SECRET must be set");

        String endpoint = defaultIfBlank(System.getenv("OSS_ENDPOINT"), DEFAULT_ENDPOINT);
        String objectKey = TARGET_PREFIX + "/cursor-live-write-check-" + System.currentTimeMillis() + ".txt";
        String expectedContent = "cursor-live-oss-write-check";

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            ossClient.putObject(TARGET_BUCKET, objectKey,
                    new ByteArrayInputStream(expectedContent.getBytes(StandardCharsets.UTF_8)));

            try (OSSObject object = ossClient.getObject(TARGET_BUCKET, objectKey);
                 InputStream stream = object.getObjectContent()) {
                String actualContent = readAll(stream);
                Assertions.assertEquals(expectedContent, actualContent);
            }
        } finally {
            try {
                ossClient.deleteObject(TARGET_BUCKET, objectKey);
            } catch (Exception ignored) {
                // Deletion cleanup failure should not hide the actual write/read assertion result.
            }
            ossClient.shutdown();
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String defaultIfBlank(String value, String defaultValue) {
        return hasText(value) ? value : defaultValue;
    }

    private static String readAll(InputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = stream.read(buffer)) >= 0) {
            output.write(buffer, 0, read);
        }
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }
}
