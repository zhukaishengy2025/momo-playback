# momo-playback

## Cloud Agent environment

This repository now includes a Cloud Agent environment config at
`.cursor/environment.json`.

Default capabilities:

- Java 8 toolchain (Temurin 8)
- Apache Maven 3.8+ (default install target: 3.9.9)
- FFmpeg
- RTMP segmented recording helper for debugging

After environment bootstrap, Java 8 Spring Boot projects can run:

```bash
mvn test
mvn package
```

RTMP segmented recording debug:

```bash
rtmp-segment-record rtmp://127.0.0.1/live/stream ./recordings 5
```
