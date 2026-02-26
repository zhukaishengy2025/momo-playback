package com.momo.playback.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "recording")
public class RecordingProperties {

    @NotBlank
    private String ffmpegPath;

    @NotBlank
    private String workDir;

    @Min(1)
    private int segmentSeconds = 60;

    @Min(1000)
    private long retryDelayMs = 3000L;

    public String getFfmpegPath() {
        return ffmpegPath;
    }

    public void setFfmpegPath(String ffmpegPath) {
        this.ffmpegPath = ffmpegPath;
    }

    public String getWorkDir() {
        return workDir;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public int getSegmentSeconds() {
        return segmentSeconds;
    }

    public void setSegmentSeconds(int segmentSeconds) {
        this.segmentSeconds = segmentSeconds;
    }

    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    public void setRetryDelayMs(long retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }
}
