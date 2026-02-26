package com.momo.playback.config;

import com.momo.playback.PlaybackApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = PlaybackApplication.class, properties = {
        "OSS_ENDPOINT=https://oss-us-west-1.aliyuncs.com",
        "OSS_TARGET_PREFIX=oss://mypifi-test/mytest",
        "OSS_ACCESS_KEY_ID=env-key-id",
        "OSS_ACCESS_KEY_SECRET=env-key-secret",
        "ACCESS_KEY_ID=legacy-key-id",
        "ACCESS_KEY_SECRET=legacy-key-secret"
})
class OssPropertiesBindingTest {

    @Autowired
    private OssProperties ossProperties;

    @Test
    void shouldBindCredentialsFromOssEnvironmentVariables() {
        Assertions.assertEquals("env-key-id", ossProperties.getAccessKeyId());
        Assertions.assertEquals("env-key-secret", ossProperties.getAccessKeySecret());
    }
}
