package com.momo.playback.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class OssObjectKeyBuilder {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private OssObjectKeyBuilder() {
    }

    public static String build(String keyPrefix, String streamKey, LocalDateTime segmentStart, int segmentSeconds) {
        String date = DATE_FORMAT.format(segmentStart);
        String timestamp = DATETIME_FORMAT.format(segmentStart);
        String filename = streamKey + "_" + timestamp + "_" + segmentSeconds + ".mp4";
        String subPath = streamKey + "/" + date + "/" + filename;
        if (keyPrefix == null || keyPrefix.isEmpty()) {
            return subPath;
        }
        return keyPrefix + "/" + subPath;
    }
}
