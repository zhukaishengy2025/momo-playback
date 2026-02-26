# AGENTS.md

## Cursor Cloud specific instructions

### Project overview

momo-playback is a Java 8 / Spring Boot 2.7.18 RTMP recording microservice. It pulls RTMP streams via FFmpeg, segments them into 60-second MP4 files, and uploads to Alibaba Cloud OSS. See `README.md` for full feature details and API usage.

### System dependencies

- **JDK 8** (`openjdk-8-jdk-headless`) — set as default via `update-alternatives` and `JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64` in `~/.bashrc`
- **Maven 3.8+** — installed via apt
- **FFmpeg** — pre-installed in the VM

### Common commands

| Task | Command |
|------|---------|
| Run tests | `mvn test` |
| Build JAR | `mvn clean package` (or `-DskipTests` to skip tests) |
| Run app | `java -jar target/playback-1.0.0.jar` |

### Running the application locally

The app requires OSS credentials to pass `@NotBlank` validation at startup. For local dev/testing without real Alibaba Cloud credentials, provide dummy values:

```bash
export OSS_ACCESS_KEY_ID=test-key-id
export OSS_ACCESS_KEY_SECRET=test-key-secret
```

The app will start and serve its REST API on port 8080. Actual OSS uploads will fail with dummy credentials, but the API endpoints and recording logic (FFmpeg invocation) will work.

### Gotchas

- The VM has both JDK 8 and JDK 21 installed. `JAVA_HOME` must point to JDK 8 (`/usr/lib/jvm/java-8-openjdk-amd64`); this is set in `~/.bashrc`. If `mvn` picks up the wrong JDK, check `java -version` and `update-alternatives`.
- There is no linter configured in this project (no Checkstyle, SpotBugs, or PMD). `mvn test` is the primary quality gate.
- No Docker or docker-compose is used. The app runs as a standalone JAR.
