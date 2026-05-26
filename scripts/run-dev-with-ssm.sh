#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/run-dev-with-ssm.sh [gradle task...]

Loads Schedly backend secrets from AWS Systems Manager Parameter Store, exports
them as Spring Boot environment variables, then starts Gradle.

Default:
  SCHEDLY_ENV=dev
  SCHEDLY_APP=schedly-be
  SSM_PARAM_PREFIX=/schedly/dev/schedly-be
  SPRING_PROFILES_ACTIVE=dev
  SERVER_PORT=8080
  Gradle task: --no-daemon bootRun

Optional examples:
  AWS_PROFILE=schedly-dev AWS_REGION=ap-northeast-2 ./scripts/run-dev-with-ssm.sh
  SERVER_PORT=18080 ./scripts/run-dev-with-ssm.sh
  SSM_PARAM_PREFIX=/schedly/dev/schedly-be ./scripts/run-dev-with-ssm.sh --no-daemon bootRun
USAGE
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

require_command() {
  local command_name="$1"

  if ! command -v "$command_name" >/dev/null 2>&1; then
    echo "Missing required command: $command_name" >&2
    exit 1
  fi
}

require_command aws

if [[ ! -x ./gradlew ]]; then
  echo "Missing executable Gradle wrapper: ./gradlew" >&2
  exit 1
fi

export AWS_PAGER=""

configured_region="$(aws configure get region 2>/dev/null || true)"
aws_region="${AWS_REGION:-${AWS_DEFAULT_REGION:-${configured_region:-ap-northeast-2}}}"
export AWS_REGION="$aws_region"
export AWS_DEFAULT_REGION="$aws_region"

schedly_env="${SCHEDLY_ENV:-dev}"
schedly_app="${SCHEDLY_APP:-schedly-be}"
param_prefix="${SSM_PARAM_PREFIX:-/schedly/${schedly_env}/${schedly_app}}"

aws_base_args=(aws)
if [[ -n "${AWS_PROFILE:-}" ]]; then
  aws_base_args+=(--profile "$AWS_PROFILE")
fi
aws_base_args+=(--region "$aws_region")

read_ssm() {
  local param_name="$1"
  local full_name="${param_prefix}/${param_name}"
  local value

  if ! value="$("${aws_base_args[@]}" ssm get-parameter \
    --name "$full_name" \
    --with-decryption \
    --query 'Parameter.Value' \
    --output text)"; then
    echo "Failed to read Parameter Store value: $full_name" >&2
    exit 1
  fi

  if [[ -z "$value" || "$value" == "None" ]]; then
    echo "Parameter Store value is empty: $full_name" >&2
    exit 1
  fi

  printf '%s' "$value"
}

echo "Loading backend configuration from Parameter Store."
echo "  prefix: $param_prefix"
echo "  region: $aws_region"
if [[ -n "${AWS_PROFILE:-}" ]]; then
  echo "  profile: $AWS_PROFILE"
else
  echo "  profile: default"
fi

export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-dev}"
export SERVER_PORT="${SERVER_PORT:-8080}"
export SPRING_DATASOURCE_URL="$(read_ssm spring.datasource.url)"
export SPRING_DATASOURCE_USERNAME="$(read_ssm spring.datasource.username)"
export SPRING_DATASOURCE_PASSWORD="$(read_ssm spring.datasource.password)"
export APP_JWT_SECRET="$(read_ssm app.jwt.secret)"

if [[ "$#" -eq 0 ]]; then
  set -- --no-daemon bootRun
fi

echo "Running Gradle with Spring profile '$SPRING_PROFILES_ACTIVE' and server port '$SERVER_PORT'."
exec ./gradlew "$@"
