#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

JAVA8_DIR="${JAVA8_DIR:-/opt/jdk8}"
MAVEN_DIR="${MAVEN_DIR:-/opt/maven}"
MAVEN_VERSION="${MAVEN_VERSION:-3.9.9}"

log() {
  echo "[cloud-env-install] $*"
}

as_root() {
  if [[ "${EUID}" -eq 0 ]]; then
    "$@"
    return
  fi

  if command -v sudo >/dev/null 2>&1; then
    sudo "$@"
    return
  fi

  echo "[cloud-env-install] ERROR: root or sudo privilege is required: $*" >&2
  exit 1
}

version_gte() {
  local actual="$1"
  local expected="$2"
  local minimum

  minimum="$(printf '%s\n%s\n' "${actual}" "${expected}" | sort -V | head -n 1)"
  [[ "${minimum}" == "${expected}" ]]
}

archive_root_dir() {
  local archive_file="$1"
  tar -tzf "${archive_file}" | awk -F/ 'NR==1 {print $1; exit}'
}

install_base_packages() {
  if command -v apt-get >/dev/null 2>&1; then
    log "Installing system dependencies (ffmpeg/curl/tar)."
    as_root apt-get update -y
    as_root env DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
      ca-certificates \
      curl \
      ffmpeg \
      gzip \
      tar
    return
  fi

  for binary in curl tar ffmpeg; do
    if ! command -v "${binary}" >/dev/null 2>&1; then
      echo "[cloud-env-install] ERROR: '${binary}' is required but no apt-get available." >&2
      exit 1
    fi
  done
}

install_java8() {
  if [[ -x "${JAVA8_DIR}/bin/java" ]]; then
    log "Java already present at ${JAVA8_DIR}."
  else
    local tmp_dir archive url extracted_dir
    tmp_dir="$(mktemp -d)"
    archive="${tmp_dir}/java8.tar.gz"
    url="https://api.adoptium.net/v3/binary/latest/8/ga/linux/x64/jdk/hotspot/normal/eclipse"

    log "Downloading Temurin Java 8 from Adoptium."
    curl -fsSL "${url}" -o "${archive}"

    extracted_dir="$(archive_root_dir "${archive}")"
    if [[ -z "${extracted_dir}" ]]; then
      echo "[cloud-env-install] ERROR: failed to detect Java archive root directory." >&2
      exit 1
    fi

    as_root rm -rf "/opt/${extracted_dir}" "${JAVA8_DIR}"
    as_root tar -xzf "${archive}" -C /opt
    as_root mv "/opt/${extracted_dir}" "${JAVA8_DIR}"
    rm -rf "${tmp_dir}"

    log "Installed Java 8 into ${JAVA8_DIR}."
  fi

  as_root ln -sfn "${JAVA8_DIR}" /opt/java8
  as_root ln -sfn "${JAVA8_DIR}/bin/java" /usr/local/bin/java8
}

install_maven() {
  if [[ -x "${MAVEN_DIR}/bin/mvn" ]]; then
    local current_version
    current_version="$("${MAVEN_DIR}/bin/mvn" -v | awk '/Apache Maven/ {print $3; exit}')"
    if [[ -n "${current_version}" ]] && version_gte "${current_version}" "3.8.0"; then
      log "Maven ${current_version} already satisfies >= 3.8."
      as_root ln -sfn "${MAVEN_DIR}/bin/mvn" /usr/local/bin/mvn
      return
    fi
  fi

  local tmp_dir archive primary_url backup_url extracted_dir
  tmp_dir="$(mktemp -d)"
  archive="${tmp_dir}/maven.tar.gz"
  primary_url="https://downloads.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz"
  backup_url="https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz"

  log "Downloading Apache Maven ${MAVEN_VERSION}."
  if ! curl -fsSL "${primary_url}" -o "${archive}"; then
    curl -fsSL "${backup_url}" -o "${archive}"
  fi

  extracted_dir="$(archive_root_dir "${archive}")"
  if [[ -z "${extracted_dir}" ]]; then
    echo "[cloud-env-install] ERROR: failed to detect Maven archive root directory." >&2
    exit 1
  fi

  as_root rm -rf "/opt/${extracted_dir}" "${MAVEN_DIR}"
  as_root tar -xzf "${archive}" -C /opt
  as_root ln -sfn "/opt/${extracted_dir}" "${MAVEN_DIR}"
  as_root ln -sfn "${MAVEN_DIR}/bin/mvn" /usr/local/bin/mvn
  rm -rf "${tmp_dir}"

  log "Installed Maven into ${MAVEN_DIR}."
}

install_profile() {
  local profile_file
  profile_file="/etc/profile.d/cloud-agent-java8-maven.sh"

  as_root tee "${profile_file}" >/dev/null <<'EOF'
export JAVA_HOME=/opt/jdk8
export MAVEN_HOME=/opt/maven

case ":$PATH:" in
  *":$JAVA_HOME/bin:"*) ;;
  *) PATH="$JAVA_HOME/bin:$PATH" ;;
esac

case ":$PATH:" in
  *":$MAVEN_HOME/bin:"*) ;;
  *) PATH="$MAVEN_HOME/bin:$PATH" ;;
esac

export PATH
EOF

  as_root chmod 0644 "${profile_file}"
  log "Wrote profile ${profile_file}."
}

install_rtmp_helper() {
  local source_script target_script
  source_script="${REPO_ROOT}/.cursor/scripts/rtmp-segment-record.sh"
  target_script="/usr/local/bin/rtmp-segment-record"

  if [[ ! -f "${source_script}" ]]; then
    echo "[cloud-env-install] ERROR: missing RTMP helper script: ${source_script}" >&2
    exit 1
  fi

  as_root install -m 0755 "${source_script}" "${target_script}"
  log "Installed RTMP helper: ${target_script}."
}

validate_installation() {
  "${JAVA8_DIR}/bin/java" -version >/dev/null 2>&1
  "${MAVEN_DIR}/bin/mvn" -v >/dev/null 2>&1
  command -v ffmpeg >/dev/null 2>&1
  command -v rtmp-segment-record >/dev/null 2>&1
}

main() {
  install_base_packages
  install_java8
  install_maven
  install_profile
  install_rtmp_helper
  validate_installation

  log "Environment setup completed (Java 8 + Maven >=3.8 + FFmpeg)."
}

main "$@"
