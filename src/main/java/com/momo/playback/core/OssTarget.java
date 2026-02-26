package com.momo.playback.core;

public class OssTarget {

    private final String bucket;
    private final String keyPrefix;

    public OssTarget(String bucket, String keyPrefix) {
        this.bucket = bucket;
        this.keyPrefix = keyPrefix;
    }

    public String getBucket() {
        return bucket;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }
}
