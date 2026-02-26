package com.momo.playback.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StreamUrlUtilsTest {

    @Test
    void shouldExtractLastPathPartAsStreamKey() {
        String streamKey = StreamUrlUtils.extractStreamKey("rtmp://a/b/c?auth_key=d");
        Assertions.assertEquals("c", streamKey);
    }

    @Test
    void shouldThrowWhenNotRtmpProtocol() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> StreamUrlUtils.validateRtmp("http://a/b/c"));
    }
}
