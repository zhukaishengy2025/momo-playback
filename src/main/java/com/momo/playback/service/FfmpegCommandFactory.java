package com.momo.playback.service;

import com.momo.playback.config.RecordingProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class FfmpegCommandFactory {

    private final RecordingProperties recordingProperties;

    public FfmpegCommandFactory(RecordingProperties recordingProperties) {
        this.recordingProperties = recordingProperties;
    }

    public List<String> build(String streamUrl, Path outputDir) {
        int segmentSeconds = recordingProperties.getSegmentSeconds();
        String segmentExpr = "expr:gte(t,n_forced*" + segmentSeconds + ")";
        String outputPattern = outputDir.resolve("%Y%m%d%H%M%S.mp4").toString();

        List<String> command = new ArrayList<>();
        command.add(recordingProperties.getFfmpegPath());
        command.add("-hide_banner");
        command.add("-loglevel");
        command.add("warning");
        command.add("-rtmp_live");
        command.add("live");
        command.add("-rw_timeout");
        command.add("15000000");
        command.add("-i");
        command.add(streamUrl);
        command.add("-map");
        command.add("0:v:0");
        command.add("-map");
        command.add("0:a:0");
        command.add("-c:v");
        command.add("libx264");
        command.add("-preset");
        command.add("veryfast");
        command.add("-tune");
        command.add("zerolatency");
        command.add("-force_key_frames");
        command.add(segmentExpr);
        command.add("-sc_threshold");
        command.add("0");
        command.add("-c:a");
        command.add("aac");
        command.add("-ar");
        command.add("48000");
        command.add("-ac");
        command.add("2");
        command.add("-f");
        command.add("segment");
        command.add("-segment_time");
        command.add(String.valueOf(segmentSeconds));
        command.add("-segment_time_delta");
        command.add("0.001");
        command.add("-break_non_keyframes");
        command.add("1");
        command.add("-reset_timestamps");
        command.add("1");
        command.add("-segment_format");
        command.add("mp4");
        command.add("-segment_format_options");
        command.add("movflags=+faststart");
        command.add("-strftime");
        command.add("1");
        command.add("-strftime_mkdir");
        command.add("1");
        command.add(outputPattern);
        return command;
    }
}
