# momo-playback

Java 8 + Spring Boot 录制服务：接收 RTMP 拉流地址，连续拉流并每 60 秒切成一个 MP4 文件，然后上传到阿里云 OSS。

## 功能说明

- 提供接口：`POST /api/recordings/start`
- 入参：RTMP 拉流地址（例如 `rtmp://a/b/c?auth_key=d`）
- 录制方式：单一 FFmpeg 进程连续分片，无人工重启导致的分片间断
- 分片长度：固定 60 秒（segment 模式 + 强制关键帧）
- 文件格式：`mp4`（包含视频 `v` 和音频 `a`）
- 拉流失败：FFmpeg 退出后自动重试，直到成功
- OSS 路径格式：
  - 路径：`路径前缀/c/yyyyMMdd/`
  - 文件名：`c_yyyyMMddHHmmss_60.mp4`

## 环境准备

1. 安装 JDK 8
2. 安装 Maven 3.8+
3. 安装 FFmpeg 并保证 `ffmpeg` 命令可执行
4. 设置环境变量（按你的测试数据）：

```bash
export test_url='rtmp://a/b/c?auth_key=d'
export OSS_ENDPOINT='https://oss-us-west-1.aliyuncs.com'
export OSS_ACCESS_KEY_ID='你的AccessKey ID'
export OSS_ACCESS_KEY_SECRET='你的AccessKey Secret'
export OSS_TARGET_PREFIX='oss://mypifi-test/mytest'
```

> 说明：操作系统环境变量名一般不能包含空格，因此将 “AccessKey ID/Secret” 映射到
> `OSS_ACCESS_KEY_ID` / `OSS_ACCESS_KEY_SECRET` 使用。

## 启动

```bash
mvn clean package
java -jar target/playback-1.0.0.jar
```

## 调用示例

```bash
curl -X POST 'http://127.0.0.1:8080/api/recordings/start' \
  -H 'Content-Type: application/json' \
  -d "{\"streamUrl\":\"${test_url}\"}"
```

返回示例：

```json
{
  "recordingId": "f0f0b48e-1b4f-4276-8be2-4de71b6f3adf",
  "streamKey": "c",
  "message": "recording started"
}
```

可选停止接口：

```bash
curl -X POST 'http://127.0.0.1:8080/api/recordings/stop/{recordingId}'
```

## 关键配置（application.yml）

```yaml
recording:
  ffmpeg-path: ${FFMPEG_PATH:ffmpeg}
  work-dir: ${RECORDING_WORK_DIR:/tmp/momo-playback}
  segment-seconds: 60
  retry-delay-ms: 3000

oss:
  endpoint: ${OSS_ENDPOINT:https://oss-us-west-1.aliyuncs.com}
  access-key-id: ${OSS_ACCESS_KEY_ID:}
  access-key-secret: ${OSS_ACCESS_KEY_SECRET:}
  target-prefix: ${OSS_TARGET_PREFIX:oss://mypifi-test/mytest}
```
