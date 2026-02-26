package com.momo.playback.service;

import com.aliyun.oss.OSS;
import com.momo.playback.config.RecordingProperties;
import com.momo.playback.core.OssObjectKeyBuilder;
import com.momo.playback.core.OssTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class RecordingWorker implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(RecordingWorker.class);
    private static final DateTimeFormatter SEGMENT_FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final String recordingId;
    private final String streamUrl;
    private final String streamKey;
    private final Path outputDir;
    private final FfmpegCommandFactory ffmpegCommandFactory;
    private final RecordingProperties recordingProperties;
    private final OSS ossClient;
    private final OssTarget ossTarget;
    private final Runnable exitCallback;

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Set<Path> uploadedFiles = new HashSet<>();
    private final Map<Path, Long> observedSizes = new HashMap<>();

    private volatile Process ffmpegProcess;

    public RecordingWorker(String recordingId,
                           String streamUrl,
                           String streamKey,
                           Path outputDir,
                           FfmpegCommandFactory ffmpegCommandFactory,
                           RecordingProperties recordingProperties,
                           OSS ossClient,
                           OssTarget ossTarget,
                           Runnable exitCallback) {
        this.recordingId = recordingId;
        this.streamUrl = streamUrl;
        this.streamKey = streamKey;
        this.outputDir = outputDir;
        this.ffmpegCommandFactory = ffmpegCommandFactory;
        this.recordingProperties = recordingProperties;
        this.ossClient = ossClient;
        this.ossTarget = ossTarget;
        this.exitCallback = exitCallback;
    }

    @Override
    public void run() {
        try {
            Files.createDirectories(outputDir);
            while (running.get()) {
                launchFfmpeg();
                superviseCurrentProcess();
                if (running.get()) {
                    sleep(recordingProperties.getRetryDelayMs());
                }
            }
            uploadCompletedSegments();
        } catch (Exception e) {
            log.error("recording {} stopped due to error", recordingId, e);
        } finally {
            destroyProcess();
            safeRunExitCallback();
        }
    }

    public void stop() {
        running.set(false);
        destroyProcess();
    }

    private void launchFfmpeg() throws IOException {
        List<String> command = ffmpegCommandFactory.build(streamUrl, outputDir);
        Path logFile = outputDir.resolve("ffmpeg.log");
        ProcessBuilder processBuilder = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.appendTo(logFile.toFile()));
        log.info("launch ffmpeg for recording {}, command={}", recordingId, String.join(" ", command));
        ffmpegProcess = processBuilder.start();
    }

    private void superviseCurrentProcess() {
        Process process = ffmpegProcess;
        if (process == null) {
            return;
        }

        while (running.get() && process.isAlive()) {
            uploadCompletedSegments();
            sleep(2000L);
        }

        uploadCompletedSegments();
        if (running.get()) {
            int exitCode = process.exitValue();
            log.warn("ffmpeg exited for recording {}, exitCode={}, will retry", recordingId, exitCode);
        }
    }

    private void uploadCompletedSegments() {
        try {
            if (!Files.exists(outputDir)) {
                return;
            }
            List<Path> candidates = Files.list(outputDir)
                    .filter(path -> path.getFileName().toString().endsWith(".mp4"))
                    .sorted(Comparator.comparing(Path::toString))
                    .collect(Collectors.toList());

            for (Path candidate : candidates) {
                if (uploadedFiles.contains(candidate)) {
                    continue;
                }
                if (!isStableFile(candidate)) {
                    continue;
                }
                uploadToOss(candidate);
                uploadedFiles.add(candidate);
                observedSizes.remove(candidate);
                Files.deleteIfExists(candidate);
            }
        } catch (Exception e) {
            log.error("upload check failed for recording {}", recordingId, e);
        }
    }

    private boolean isStableFile(Path file) throws IOException {
        long size = Files.size(file);
        if (size <= 0) {
            return false;
        }
        long now = Instant.now().toEpochMilli();
        long lastModified = Files.getLastModifiedTime(file).toMillis();
        if (now - lastModified < 1500) {
            observedSizes.put(file, size);
            return false;
        }
        Long previousSize = observedSizes.put(file, size);
        return previousSize != null && previousSize == size;
    }

    private void uploadToOss(Path file) {
        LocalDateTime segmentStart = parseSegmentStart(file);
        String objectKey = OssObjectKeyBuilder.build(
                ossTarget.getKeyPrefix(),
                streamKey,
                segmentStart,
                recordingProperties.getSegmentSeconds()
        );
        log.info("upload segment to oss, recordingId={}, bucket={}, key={}, file={}",
                recordingId, ossTarget.getBucket(), objectKey, file);
        ossClient.putObject(ossTarget.getBucket(), objectKey, file.toFile());
    }

    private LocalDateTime parseSegmentStart(Path file) {
        String fileName = file.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String pureName = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
        try {
            return LocalDateTime.parse(pureName, SEGMENT_FILE_TIME);
        } catch (Exception ignored) {
            try {
                return LocalDateTime.ofInstant(Files.getLastModifiedTime(file).toInstant(), ZoneId.systemDefault());
            } catch (IOException e) {
                return LocalDateTime.now();
            }
        }
    }

    private void destroyProcess() {
        Process process = ffmpegProcess;
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
    }

    private void sleep(long durationMs) {
        try {
            Thread.sleep(durationMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            running.set(false);
        }
    }

    private void safeRunExitCallback() {
        try {
            exitCallback.run();
        } catch (Exception e) {
            log.warn("exit callback failed for recording {}", recordingId, e);
        }
    }
}
