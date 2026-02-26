package com.momo.playback.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class OssObjectKeyBuilderTest {

    @Test
    void shouldGenerateExpectedObjectKey() {
        LocalDateTime time = LocalDateTime.of(2026, 2, 26, 18, 30, 10);
        String key = OssObjectKeyBuilder.build("mytest", "c", time, 60);
        Assertions.assertEquals("mytest/c/20260226/c_20260226183010_60.mp4", key);
    }
}
