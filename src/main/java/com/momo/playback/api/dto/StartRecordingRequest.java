package com.momo.playback.api.dto;

import javax.validation.constraints.NotBlank;

public class StartRecordingRequest {

    @NotBlank(message = "streamUrl cannot be blank")
    private String streamUrl;

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }
}
