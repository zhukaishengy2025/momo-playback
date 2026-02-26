package com.momo.playback.api;

import com.momo.playback.api.dto.StartRecordingRequest;
import com.momo.playback.api.dto.StartRecordingResponse;
import com.momo.playback.service.RecordingService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/recordings")
public class RecordingController {

    private final RecordingService recordingService;

    public RecordingController(RecordingService recordingService) {
        this.recordingService = recordingService;
    }

    @PostMapping("/start")
    public ResponseEntity<StartRecordingResponse> start(@Valid @RequestBody StartRecordingRequest request) {
        StartRecordingResponse response = recordingService.start(request.getStreamUrl().trim());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/stop/{recordingId}")
    public ResponseEntity<Void> stop(@PathVariable("recordingId") String recordingId) {
        recordingService.stop(recordingId);
        return ResponseEntity.ok().build();
    }
}
