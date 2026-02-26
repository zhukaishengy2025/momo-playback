package com.momo.playback.core;

import java.net.URI;
import java.util.Locale;

public final class StreamUrlUtils {

    private StreamUrlUtils() {
    }

    public static void validateRtmp(String streamUrl) {
        URI uri = URI.create(streamUrl);
        if (uri.getScheme() == null || !"rtmp".equals(uri.getScheme().toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("streamUrl must use rtmp protocol");
        }
        if (uri.getPath() == null || uri.getPath().trim().isEmpty()) {
            throw new IllegalArgumentException("streamUrl path cannot be empty");
        }
    }

    public static String extractStreamKey(String streamUrl) {
        URI uri = URI.create(streamUrl);
        String path = uri.getPath();
        if (path == null) {
            throw new IllegalArgumentException("streamUrl path cannot be empty");
        }
        String[] sections = path.split("/");
        for (int i = sections.length - 1; i >= 0; i--) {
            if (!sections[i].trim().isEmpty()) {
                return sanitize(sections[i]);
            }
        }
        throw new IllegalArgumentException("cannot extract stream key from streamUrl");
    }

    private static String sanitize(String source) {
        String result = source.replaceAll("[^A-Za-z0-9_-]", "_");
        if (result.isEmpty()) {
            throw new IllegalArgumentException("stream key from streamUrl is invalid");
        }
        return result;
    }
}
