package com.momo.playback.api.dto;

public class StartRecordingResponse {

    private String recordingId;
    private String streamKey;
    private String message;

    public StartRecordingResponse() {
    }

    public StartRecordingResponse(String recordingId, String streamKey, String message) {
        this.recordingId = recordingId;
        this.streamKey = streamKey;
        this.message = message;
    }

    public String getRecordingId() {
        return recordingId;
    }

    public void setRecordingId(String recordingId) {
        this.recordingId = recordingId;
    }

    public String getStreamKey() {
        return streamKey;
    }

    public void setStreamKey(String streamKey) {
        this.streamKey = streamKey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
