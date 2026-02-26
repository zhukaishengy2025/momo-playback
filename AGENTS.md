# AGENTS.md

## Cursor Cloud specific instructions

### Overview

**momo-playback** is a Java 8 / Spring Boot 2.7.18 backend service that records RTMP streams via FFmpeg and uploads segments to Alibaba Cloud OSS. No database, no frontend.

### Prerequisites (system-level, already installed on cloud VM)

- JDK 8 (`JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64`)
- Maven 3.8+ (system package)
- FFmpeg (system package, must be on `$PATH`)

### Environment variables

`JAVA_HOME` and `PATH` are configured in `~/.bashrc` to use JDK 8. If a new shell doesn't pick them up, run:

```bash
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export PATH="/usr/lib/jvm/java-8-openjdk-amd64/bin:$PATH"
```

The app requires `OSS_ACCESS_KEY_ID` and `OSS_ACCESS_KEY_SECRET` to start (validated as `@NotBlank`). For local dev without real OSS access, use placeholder values:

```bash
OSS_ACCESS_KEY_ID=dev-placeholder OSS_ACCESS_KEY_SECRET=dev-placeholder java -jar target/playback-1.0.0.jar
```

### Key commands

| Task | Command |
|------|---------|
| Unit tests | `mvn test` |
| Build JAR | `mvn clean package` |
| Build (skip tests) | `mvn clean package -DskipTests` |
| Run app (dev) | `OSS_ACCESS_KEY_ID=dev-placeholder OSS_ACCESS_KEY_SECRET=dev-placeholder java -jar target/playback-1.0.0.jar` |
| Start recording | `curl -X POST 'http://127.0.0.1:8080/api/recordings/start' -H 'Content-Type: application/json' -d '{"streamUrl":"rtmp://..."}'` |
| Stop recording | `curl -X POST 'http://127.0.0.1:8080/api/recordings/stop/{recordingId}'` |

### Gotchas

- The VM has both JDK 21 (default) and JDK 8 installed. Maven and the app **must** use JDK 8 — the `pom.xml` sets `java.version=1.8`. Always ensure `JAVA_HOME` points to JDK 8 before running Maven or the JAR.
- There is no lint tool configured in this project (no Checkstyle, SpotBugs, or PMD). Compilation (`mvn compile`) serves as the primary static check.
- The app exits quickly after a recording stop because the FFmpeg subprocess terminates. This is expected behavior in dev without a real RTMP stream.
- No `.pre-commit-config.yaml`, no Husky hooks, no lint-staged — no git hooks exist.
