#!/usr/bin/env bash
set -euo pipefail

PROFILE_FILE="/etc/profile.d/cloud-agent-java8-maven.sh"

if [[ -f "${PROFILE_FILE}" ]]; then
  # shellcheck disable=SC1091
  source "${PROFILE_FILE}"
fi

echo "[cloud-env-start] Java:"
if command -v java >/dev/null 2>&1; then
  java -version 2>&1 | sed -n '1p'
else
  echo "java not found"
fi

echo "[cloud-env-start] Maven:"
if command -v mvn >/dev/null 2>&1; then
  mvn -v | sed -n '1,2p'
else
  echo "mvn not found"
fi

echo "[cloud-env-start] FFmpeg:"
if command -v ffmpeg >/dev/null 2>&1; then
  ffmpeg -version | sed -n '1p'
else
  echo "ffmpeg not found"
fi

echo "[cloud-env-start] RTMP helper: use 'rtmp-segment-record <rtmp_url> <output_dir> [segment_seconds]'"
