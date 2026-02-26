package com.momo.playback.core;

import java.net.URI;
import java.util.Locale;

public final class OssTargetParser {

    private OssTargetParser() {
    }

    public static OssTarget parse(String targetPrefix) {
        URI uri = URI.create(targetPrefix);
        if (uri.getScheme() == null || !"oss".equals(uri.getScheme().toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("oss.target-prefix must use oss:// scheme");
        }
        String bucket = uri.getHost();
        if (bucket == null || bucket.trim().isEmpty()) {
            throw new IllegalArgumentException("oss.target-prefix bucket is empty");
        }
        String path = uri.getPath() == null ? "" : uri.getPath();
        String normalized = normalize(path);
        return new OssTarget(bucket, normalized);
    }

    private static String normalize(String value) {
        String normalized = value.replaceAll("^/+", "").replaceAll("/+$", "");
        return normalized.trim();
    }
}
