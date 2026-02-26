#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage:
  rtmp-segment-record <rtmp_url> <output_dir> [segment_seconds]

Example:
  rtmp-segment-record rtmp://127.0.0.1/live/stream ./recordings 5
EOF
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

if [[ $# -lt 2 ]]; then
  usage >&2
  exit 1
fi

RTMP_URL="$1"
OUTPUT_DIR="$2"
SEGMENT_SECONDS="${3:-5}"

mkdir -p "${OUTPUT_DIR}"

echo "[rtmp-segment-record] Recording from: ${RTMP_URL}"
echo "[rtmp-segment-record] Segment duration: ${SEGMENT_SECONDS}s"
echo "[rtmp-segment-record] Output directory: ${OUTPUT_DIR}"

exec ffmpeg \
  -hide_banner \
  -loglevel info \
  -rtmp_live live \
  -i "${RTMP_URL}" \
  -map 0 \
  -c copy \
  -f segment \
  -segment_time "${SEGMENT_SECONDS}" \
  -segment_format flv \
  -reset_timestamps 1 \
  -strftime 1 \
  "${OUTPUT_DIR}/segment-%Y%m%d-%H%M%S.flv"
