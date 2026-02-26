package com.momo.playback.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

class OssPropertiesBindingTest {

    @Test
    void shouldOnlyUseOssCredentialEnvironmentVariablesInConfig() throws IOException {
        String applicationYaml = readResourceAsString("application.yml");

        Assertions.assertTrue(applicationYaml.contains("access-key-id: ${OSS_ACCESS_KEY_ID:}"));
        Assertions.assertTrue(applicationYaml.contains("access-key-secret: ${OSS_ACCESS_KEY_SECRET:}"));
        Assertions.assertFalse(applicationYaml.contains("${OSS_ACCESS_KEY_ID:${ACCESS_KEY_ID:}}"));
        Assertions.assertFalse(applicationYaml.contains("${OSS_ACCESS_KEY_SECRET:${ACCESS_KEY_SECRET:}}"));
    }

    private static String readResourceAsString(String resourceName) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourceName);
        try (InputStream inputStream = resource.getInputStream()) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int read;
            while ((read = inputStream.read(buffer)) >= 0) {
                output.write(buffer, 0, read);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
