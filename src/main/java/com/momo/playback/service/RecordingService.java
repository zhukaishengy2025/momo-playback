package com.momo.playback.service;

import com.aliyun.oss.OSS;
import com.momo.playback.api.dto.StartRecordingResponse;
import com.momo.playback.config.OssProperties;
import com.momo.playback.config.RecordingProperties;
import com.momo.playback.core.OssTarget;
import com.momo.playback.core.OssTargetParser;
import com.momo.playback.core.StreamUrlUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
public class RecordingService {

    private final RecordingProperties recordingProperties;
    private final OssProperties ossProperties;
    private final FfmpegCommandFactory ffmpegCommandFactory;
    private final OSS ossClient;
    private final ExecutorService recordingExecutorService;

    private final Map<String, ActiveRecording> activeByStreamUrl = new ConcurrentHashMap<>();
    private final Map<String, ActiveRecording> activeById = new ConcurrentHashMap<>();

    public RecordingService(RecordingProperties recordingProperties,
                            OssProperties ossProperties,
                            FfmpegCommandFactory ffmpegCommandFactory,
                            OSS ossClient,
                            ExecutorService recordingExecutorService) {
        this.recordingProperties = recordingProperties;
        this.ossProperties = ossProperties;
        this.ffmpegCommandFactory = ffmpegCommandFactory;
        this.ossClient = ossClient;
        this.recordingExecutorService = recordingExecutorService;
    }

    public synchronized StartRecordingResponse start(String streamUrl) {
        StreamUrlUtils.validateRtmp(streamUrl);
        ActiveRecording existing = activeByStreamUrl.get(streamUrl);
        if (existing != null && !existing.future.isDone()) {
            return new StartRecordingResponse(
                    existing.recordingId,
                    existing.streamKey,
                    "recording already started"
            );
        }

        String streamKey = StreamUrlUtils.extractStreamKey(streamUrl);
        String recordingId = UUID.randomUUID().toString();
        OssTarget ossTarget = OssTargetParser.parse(ossProperties.getTargetPrefix());
        Path outputDir = prepareOutputDir(streamKey, recordingId);

        RecordingWorker worker = new RecordingWorker(
                recordingId,
                streamUrl,
                streamKey,
                outputDir,
                ffmpegCommandFactory,
                recordingProperties,
                ossClient,
                ossTarget,
                () -> removeActive(streamUrl, recordingId)
        );
        Future<?> future = recordingExecutorService.submit(worker);

        ActiveRecording activeRecording = new ActiveRecording(recordingId, streamKey, streamUrl, worker, future);
        activeByStreamUrl.put(streamUrl, activeRecording);
        activeById.put(recordingId, activeRecording);

        return new StartRecordingResponse(recordingId, streamKey, "recording started");
    }

    public synchronized void stop(String recordingId) {
        ActiveRecording activeRecording = activeById.get(recordingId);
        if (activeRecording == null) {
            return;
        }
        activeRecording.worker.stop();
        activeRecording.future.cancel(true);
        removeActive(activeRecording.streamUrl, recordingId);
    }

    @PreDestroy
    public synchronized void shutdownAll() {
        for (ActiveRecording activeRecording : activeById.values()) {
            activeRecording.worker.stop();
            activeRecording.future.cancel(true);
        }
        activeById.clear();
        activeByStreamUrl.clear();
    }

    private Path prepareOutputDir(String streamKey, String recordingId) {
        Path dir = Paths.get(recordingProperties.getWorkDir(), streamKey, recordingId);
        try {
            Files.createDirectories(dir);
            return dir;
        } catch (IOException e) {
            throw new IllegalStateException("cannot create local output dir: " + dir, e);
        }
    }

    private synchronized void removeActive(String streamUrl, String recordingId) {
        ActiveRecording byStream = activeByStreamUrl.get(streamUrl);
        if (byStream != null && byStream.recordingId.equals(recordingId)) {
            activeByStreamUrl.remove(streamUrl);
        }
        ActiveRecording byId = activeById.get(recordingId);
        if (byId != null && byId.streamUrl.equals(streamUrl)) {
            activeById.remove(recordingId);
        }
    }

    private static class ActiveRecording {
        private final String recordingId;
        private final String streamKey;
        private final String streamUrl;
        private final RecordingWorker worker;
        private final Future<?> future;

        private ActiveRecording(String recordingId,
                                String streamKey,
                                String streamUrl,
                                RecordingWorker worker,
                                Future<?> future) {
            this.recordingId = recordingId;
            this.streamKey = streamKey;
            this.streamUrl = streamUrl;
            this.worker = worker;
            this.future = future;
        }
    }
}
