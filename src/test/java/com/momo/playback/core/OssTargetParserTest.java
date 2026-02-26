package com.momo.playback.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OssTargetParserTest {

    @Test
    void shouldParseBucketAndPrefix() {
        OssTarget target = OssTargetParser.parse("oss://mypifi-test/mytest");
        Assertions.assertEquals("mypifi-test", target.getBucket());
        Assertions.assertEquals("mytest", target.getKeyPrefix());
    }

    @Test
    void shouldAllowRootPrefix() {
        OssTarget target = OssTargetParser.parse("oss://mypifi-test");
        Assertions.assertEquals("mypifi-test", target.getBucket());
        Assertions.assertEquals("", target.getKeyPrefix());
    }
}
