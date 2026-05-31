#!/usr/bin/env bash
# =============================================================================
#  Curriculum Tracking System — End-to-End Azure App Service Deployment
# =============================================================================
#  Steps (in order):
#    1.  Pre-flight checks  (az CLI, Java, ./mvnw, Azure login, keys.properties)
#    2.  Load all values from keys.properties
#    3.  Create Resource Group
#    4.  Create App Service Plan (Linux, B1) — tries region fallback list
#    5.  Create Java 17 Web App
#    6.  Set all App Settings directly (env vars injected at runtime)
#    7.  Set Java startup command + enable filesystem logging
#    8.  Maven build  (./mvnw clean package -DskipTests)
#    9.  Deploy JAR via  az webapp deploy
#    10. Health-check poll with retry
#    11. Print deployment summary + useful commands
#
#  Usage:
#    ./deploy-azure.sh                  — full end-to-end deploy
#    ./deploy-azure.sh --skip-build     — skip Maven build, deploy existing JAR
#    ./deploy-azure.sh --skip-infra     — skip infra, only build + deploy JAR
#    ./deploy-azure.sh --skip-build --skip-infra  — (re-)deploy existing JAR only
# =============================================================================

set -euo pipefail

# ─────────────────────────────────────────────────────────────────────────────
#  TERMINAL COLOURS
# ─────────────────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
WHITE='\033[1;37m'
BOLD='\033[1m'
DIM='\033[2m'
NC='\033[0m'

# ─────────────────────────────────────────────────────────────────────────────
#  LOGGING HELPERS
# ─────────────────────────────────────────────────────────────────────────────
_STEP=0

banner() {
  echo -e "${BOLD}${BLUE}"
  echo "  ╔══════════════════════════════════════════════════════════════╗"
  echo "  ║        Curriculum Tracking System — Azure Deployment         ║"
  echo "  ║              Spring Boot 3 → Azure App Service               ║"
  echo "  ╚══════════════════════════════════════════════════════════════╝"
  echo -e "${NC}"
}

section() {
  echo -e "\n${BOLD}${MAGENTA}══════════════════════════════════════════════════════════════${NC}"
  echo -e "${BOLD}${MAGENTA}  $1${NC}"
  echo -e "${BOLD}${MAGENTA}══════════════════════════════════════════════════════════════${NC}"
}

step() {
  _STEP=$((_STEP + 1))
  echo -e "\n${BOLD}${BLUE}[STEP ${_STEP}]${NC} ${BOLD}$1${NC}"
}

info()  { echo -e "    ${CYAN}→${NC}  $1"; }
ok()    { echo -e "    ${GREEN}✔${NC}  $1"; }
warn()  { echo -e "    ${YELLOW}⚠${NC}  $1"; }
skip()  { echo -e "    ${DIM}↷  $1 — already exists, skipping${NC}"; }
die()   { echo -e "\n    ${RED}✖  FATAL: $1${NC}\n"; exit 1; }
label() {
  local k="$1"; local v="$2"
  printf "    ${WHITE}${BOLD}%-22s${NC}${CYAN}%s${NC}\n" "${k}:" "${v}"
}

# ─────────────────────────────────────────────────────────────────────────────
#  CONFIGURATION — edit these before running
# ─────────────────────────────────────────────────────────────────────────────

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KEYS_FILE="${SCRIPT_DIR}/keys.properties"

# ── Azure resource names ──────────────────────────────────────────────────────
RESOURCE_GROUP="curriculum-tracking-rg"

# App Service Plan + Web App
APP_NAME="curriculum-tracking-api"   # globally unique → <name>.azurewebsites.net
APP_SERVICE_PLAN="curriculum-plan"
SKU="B1"    # B1 ≈ $13/mo. Use F1 for free tier (cold-start caveat applies).

# Spring profile that selects application-azure.yml
SPRING_PROFILE="azure"

# JVM heap tuning for B1 (1.75 GB RAM): 512 MB min, 1.2 GB max leaves room for OS
JAVA_OPTS="-Xms512m -Xmx1228m"

# Ordered regions to try — Azure for Students restricts certain resource types
# to specific regions. The first one that succeeds wins.
REGION_CANDIDATES=("eastus" "eastus2" "westus2" "westus" "centralus" "northeurope" "westeurope" "southafricanorth")

# Resolved at runtime
APP_LOCATION=""

# Maven wrapper / command (resolved in preflight)
MVN=""

# JAR path (resolved after build or glob)
JAR_PATH=""
JAR_GLOB="${SCRIPT_DIR}/target/curriculum-tracking-system-*.jar"

# ── CLI Flags ─────────────────────────────────────────────────────────────────
SKIP_BUILD=false
SKIP_INFRA=false

for arg in "$@"; do
  case $arg in
    --skip-build) SKIP_BUILD=true ;;
    --skip-infra) SKIP_INFRA=true ;;
  esac
done

# ─────────────────────────────────────────────────────────────────────────────
#  UTILITIES
# ─────────────────────────────────────────────────────────────────────────────

# Read a value from keys.properties (skips comment lines)
prop() {
  grep -E "^${1}=" "${KEYS_FILE}" | head -1 | cut -d'=' -f2-
}

# Return 0 if az command succeeds (resource exists), 1 otherwise
az_exists() { "$@" &>/dev/null && return 0 || return 1; }

# ─────────────────────────────────────────────────────────────────────────────
#  STEP 1 — PRE-FLIGHT CHECKS
# ─────────────────────────────────────────────────────────────────────────────
preflight() {
  section "PRE-FLIGHT CHECKS"

  step "Verifying required tools"

  command -v az &>/dev/null || die "Azure CLI not found. Install: https://aka.ms/installazurecli"
  ok "az    $(az version --query '"azure-cli"' -o tsv)"

  if [[ -x "${SCRIPT_DIR}/mvnw" ]]; then
    MVN="${SCRIPT_DIR}/mvnw"
  elif command -v mvn &>/dev/null; then
    MVN="mvn"
  else
    die "Maven not found. No ./mvnw wrapper and no global mvn on PATH."
  fi
  ok "mvn   $(${MVN} --version 2>&1 | head -1 | sed 's/Apache Maven //')"

  command -v java &>/dev/null || die "Java not found. Install Java 17+."
  ok "java  $(java -version 2>&1 | head -1)"

  command -v curl &>/dev/null || die "curl not found (needed for health-check)."
  ok "curl  $(curl --version | head -1)"

  step "Verifying Azure login"
  local acct sub_id
  acct=$(az account show --query "name" -o tsv 2>/dev/null) \
    || die "Not logged in to Azure. Run: az login"
  sub_id=$(az account show --query "id" -o tsv)
  ok "Authenticated"
  label "Subscription" "${acct}"
  label "Subscription ID" "${sub_id}"

  step "Verifying keys.properties"
  [[ -f "${KEYS_FILE}" ]] || die "keys.properties not found at: ${KEYS_FILE}"
  ok "keys.properties found"
}

# ─────────────────────────────────────────────────────────────────────────────
#  STEP 2 — LOAD SECRETS
# ─────────────────────────────────────────────────────────────────────────────
load_secrets() {
  section "LOADING SECRETS FROM keys.properties"
  step "Parsing all values"

  DB_URL=$(prop "db.url")
  DB_USERNAME=$(prop "db.username")
  DB_PASSWORD=$(prop "db.password")
  MAIL_USERNAME=$(prop "mail.username")
  MAIL_PASSWORD=$(prop "mail.password")
  JWT_SECRET=$(prop "jwt.secret")
  JWT_EXPIRATION_MS=$(prop "jwt.expiration.ms")
  JWT_REFRESH_EXPIRATION_MS=$(prop "jwt.refresh.expiration.ms")
  ADMIN_USERNAME=$(prop "admin.username")
  ADMIN_EMAIL=$(prop "admin.email")
  ADMIN_PASSWORD=$(prop "admin.password")
  REDIS_HOST=$(prop "redis.host")
  REDIS_PORT=$(prop "redis.port")
  REDIS_PASSWORD=$(prop "redis.password")
  REDIS_DATABASE=$(prop "redis.database")
  REDIS_SSL_ENABLED=$(prop "redis.ssl.enabled")
  AWS_ACCESS_KEY_ID=$(prop "aws.access.key")
  AWS_SECRET_ACCESS_KEY=$(prop "aws.secret.key")
  AWS_S3_BUCKET=$(prop "aws.bucket.name")
  AWS_REGION=$(prop "aws.region.name")

  [[ -n "${DB_URL}" ]]         || die "db.url missing from keys.properties"
  [[ -n "${DB_PASSWORD}" ]]    || die "db.password missing from keys.properties"
  [[ -n "${JWT_SECRET}" ]]     || die "jwt.secret missing from keys.properties"
  [[ -n "${REDIS_HOST}" ]]     || die "redis.host missing from keys.properties"
  [[ -n "${REDIS_PASSWORD}" ]] || die "redis.password missing from keys.properties"

  ok "All 20 values loaded"
  label "DB host"    "$(echo "${DB_URL}" | sed 's|jdbc:postgresql://||' | cut -d'/' -f1)"
  label "Redis"      "${REDIS_HOST}:${REDIS_PORT}  ssl=${REDIS_SSL_ENABLED}"
  label "AWS"        "${AWS_REGION} / s3://${AWS_S3_BUCKET}"
}

# ─────────────────────────────────────────────────────────────────────────────
#  STEP 3 — RESOURCE GROUP
# ─────────────────────────────────────────────────────────────────────────────
create_resource_group() {
  section "AZURE INFRASTRUCTURE"
  step "Resource Group: ${RESOURCE_GROUP}"

  if az_exists az group show --name "${RESOURCE_GROUP}"; then
    APP_LOCATION=$(az group show --name "${RESOURCE_GROUP}" --query "location" -o tsv)
    skip "Resource group '${RESOURCE_GROUP}' (location: ${APP_LOCATION})"
    return 0
  fi

  # Try each candidate region until one is accepted by the subscription policy
  local created=false
  for region in "${REGION_CANDIDATES[@]}"; do
    info "Trying region: ${region} ..."
    local out
    out=$(az group create --name "${RESOURCE_GROUP}" --location "${region}" --output none 2>&1) \
      && created=true && APP_LOCATION="${region}" && break || true

    if echo "${out}" | grep -qi "DisallowedByAzure\|RequestDisallowedByAzure\|disallowed by policy"; then
      warn "Region '${region}' blocked by policy — trying next..."
    else
      echo "${out}"
      die "Resource group creation failed in ${region} for an unexpected reason."
    fi
  done

  [[ "${created}" == "false" ]] \
    && die "Could not create resource group in any region: ${REGION_CANDIDATES[*]}"
  ok "Resource group '${RESOURCE_GROUP}' created in ${APP_LOCATION}"
}

# ─────────────────────────────────────────────────────────────────────────────
#  STEP 4 — APP SERVICE PLAN
# ─────────────────────────────────────────────────────────────────────────────
create_app_service_plan() {
  step "App Service Plan: ${APP_SERVICE_PLAN} (Linux ${SKU})"

  if az_exists az appservice plan show --name "${APP_SERVICE_PLAN}" --resource-group "${RESOURCE_GROUP}"; then
    APP_LOCATION=$(az appservice plan show \
      --name "${APP_SERVICE_PLAN}" --resource-group "${RESOURCE_GROUP}" \
      --query "location" -o tsv)
    skip "App Service Plan '${APP_SERVICE_PLAN}' (location: ${APP_LOCATION})"
    return 0
  fi

  # Try region candidates — prefer the one the resource group is already in
  local ordered_regions=("${APP_LOCATION}" "${REGION_CANDIDATES[@]}")
  local created=false
  for region in "${ordered_regions[@]}"; do
    [[ -z "${region}" ]] && continue
    info "Trying region: ${region} ..."
    local out
    out=$(az appservice plan create \
      --name "${APP_SERVICE_PLAN}" \
      --resource-group "${RESOURCE_GROUP}" \
      --location "${region}" \
      --sku "${SKU}" \
      --is-linux \
      --output none 2>&1) \
      && created=true && APP_LOCATION="${region}" && break || true

    if echo "${out}" | grep -qi "DisallowedByAzure\|RequestDisallowedByAzure\|disallowed by policy"; then
      warn "Region '${region}' blocked — trying next..."
    else
      echo "${out}"
      die "App Service Plan creation failed in ${region} for an unexpected reason."
    fi
  done

  [[ "${created}" == "false" ]] && die "Could not create App Service Plan in any allowed region."
  ok "App Service Plan created (Linux ${SKU}) in ${APP_LOCATION}"
}

# ─────────────────────────────────────────────────────────────────────────────
#  STEP 5 — WEB APP
# ─────────────────────────────────────────────────────────────────────────────
create_web_app() {
  step "Web App: ${APP_NAME}"

  if az_exists az webapp show --name "${APP_NAME}" --resource-group "${RESOURCE_GROUP}"; then
    skip "Web App '${APP_NAME}'"
  else
    az webapp create \
      --name "${APP_NAME}" \
      --resource-group "${RESOURCE_GROUP}" \
      --plan "${APP_SERVICE_PLAN}" \
      --runtime "JAVA:17-java17" \
      --output none
    ok "Web App '${APP_NAME}' created (Java 17, region: ${APP_LOCATION})"
  fi
  label "App URL" "https://${APP_NAME}.azurewebsites.net"
}

# ─────────────────────────────────────────────────────────────────────────────
#  STEP 6 — APP SETTINGS (all values injected directly as environment variables)
# ─────────────────────────────────────────────────────────────────────────────
configure_app_settings() {
  step "Configuring App Settings (20 env vars + runtime settings)"

  az webapp config appsettings set \
    --name "${APP_NAME}" \
    --resource-group "${RESOURCE_GROUP}" \
    --output none \
    --settings \
      SPRING_PROFILES_ACTIVE="${SPRING_PROFILE}" \
      WEBSITES_PORT="80" \
      SCM_DO_BUILD_DURING_DEPLOYMENT="false" \
      JAVA_OPTS="${JAVA_OPTS}" \
      \
      DB_URL="${DB_URL}" \
      DB_USERNAME="${DB_USERNAME}" \
      DB_PASSWORD="${DB_PASSWORD}" \
      \
      MAIL_USERNAME="${MAIL_USERNAME}" \
      MAIL_PASSWORD="${MAIL_PASSWORD}" \
      \
      JWT_SECRET="${JWT_SECRET}" \
      JWT_EXPIRATION_MS="${JWT_EXPIRATION_MS}" \
      JWT_REFRESH_EXPIRATION_MS="${JWT_REFRESH_EXPIRATION_MS}" \
      \
      ADMIN_USERNAME="${ADMIN_USERNAME}" \
      ADMIN_EMAIL="${ADMIN_EMAIL}" \
      ADMIN_PASSWORD="${ADMIN_PASSWORD}" \
      \
      REDIS_HOST="${REDIS_HOST}" \
      REDIS_PORT="${REDIS_PORT}" \
      REDIS_PASSWORD="${REDIS_PASSWORD}" \
      REDIS_DATABASE="${REDIS_DATABASE}" \
      REDIS_SSL_ENABLED="${REDIS_SSL_ENABLED}" \
      \
      AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID}" \
      AWS_SECRET_ACCESS_KEY="${AWS_SECRET_ACCESS_KEY}" \
      AWS_S3_BUCKET="${AWS_S3_BUCKET}" \
      AWS_REGION="${AWS_REGION}"

  ok "App Settings saved (24 settings)"
}

# ─────────────────────────────────────────────────────────────────────────────
#  STEP 7 — STARTUP COMMAND + LOGGING
# ─────────────────────────────────────────────────────────────────────────────
configure_runtime() {
  step "Setting Java startup command"

  az webapp config set \
    --name "${APP_NAME}" \
    --resource-group "${RESOURCE_GROUP}" \
    --startup-file "java -Dspring.profiles.active=${SPRING_PROFILE} -jar /home/site/wwwroot/app.jar" \
    --output none
  ok "Startup: java -Dspring.profiles.active=${SPRING_PROFILE} -jar /home/site/wwwroot/app.jar"

  step "Enabling filesystem logging"
  az webapp log config \
    --name "${APP_NAME}" \
    --resource-group "${RESOURCE_GROUP}" \
    --web-server-logging filesystem \
    --docker-container-logging filesystem \
    --output none
  ok "Logging enabled"
}

# ─────────────────────────────────────────────────────────────────────────────
#  STEP 8 — MAVEN BUILD
# ─────────────────────────────────────────────────────────────────────────────
build_jar() {
  section "BUILD"
  step "Maven clean package (tests skipped for deploy speed)"
  info "Run './mvnw test' separately to run the full test suite."

  cd "${SCRIPT_DIR}"
  ${MVN} clean package -DskipTests --no-transfer-progress

  JAR_PATH=$(ls ${JAR_GLOB} 2>/dev/null | grep -v sources | head -1)
  [[ -z "${JAR_PATH}" ]] && die "JAR not found after build — check Maven output above."

  local size
  size=$(du -sh "${JAR_PATH}" | cut -f1)
  ok "Build complete: $(basename "${JAR_PATH}") (${size})"
}

# ─────────────────────────────────────────────────────────────────────────────
#  STEP 9 — DEPLOY JAR
# ─────────────────────────────────────────────────────────────────────────────
deploy_jar() {
  section "DEPLOYMENT"
  step "Uploading JAR to Azure App Service"

  if [[ -z "${JAR_PATH}" ]]; then
    JAR_PATH=$(ls ${JAR_GLOB} 2>/dev/null | grep -v sources | head -1)
    [[ -z "${JAR_PATH}" ]] && die "No JAR at ${JAR_GLOB}. Run without --skip-build first."
  fi

  local size
  size=$(du -sh "${JAR_PATH}" | cut -f1)
  label "File"        "$(basename "${JAR_PATH}") (${size})"
  label "Target"      "https://${APP_NAME}.azurewebsites.net"

  az webapp deploy \
    --name "${APP_NAME}" \
    --resource-group "${RESOURCE_GROUP}" \
    --src-path "${JAR_PATH}" \
    --type jar \
    --async false \
    --output none

  ok "JAR deployed — App Service is starting the application..."
}

# ─────────────────────────────────────────────────────────────────────────────
#  STEP 10 — HEALTH CHECK POLL
# ─────────────────────────────────────────────────────────────────────────────
health_check() {
  section "HEALTH CHECK"
  step "Waiting for application to respond"

  local base_url="https://${APP_NAME}.azurewebsites.net"
  local health_url="${base_url}/actuator/health"
  local max=24   # 24 × 15 s = 6 min
  local delay=15
  local attempt=0

  label "Endpoint" "${health_url}"
  info  "Polling every ${delay}s (max $((max * delay / 60)) min)..."
  echo ""

  while [[ ${attempt} -lt ${max} ]]; do
    attempt=$((attempt + 1))
    local code
    code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 10 "${health_url}" 2>/dev/null \
           || echo "000")

    printf "    ${DIM}[%02d/%02d]${NC}  HTTP %-5s" "${attempt}" "${max}" "${code}"

    case "${code}" in
      200)
        echo -e "  ${GREEN}← UP ✔${NC}"
        ok "Application is healthy!"
        return 0
        ;;
      401|403)
        echo -e "  ${YELLOW}← secured — app is running ✔${NC}"
        ok "Application is running (health endpoint requires auth — this is fine)"
        return 0
        ;;
      000)
        echo -e "  ${DIM}← not yet reachable${NC}"
        ;;
      *)
        echo -e "  ${DIM}← starting (${code})${NC}"
        ;;
    esac
    sleep "${delay}"
  done

  echo ""
  warn "App did not respond within the timeout — it may still be starting up."
  warn "Stream logs:  az webapp log tail --name ${APP_NAME} --resource-group ${RESOURCE_GROUP}"
}

# ─────────────────────────────────────────────────────────────────────────────
#  DEPLOYMENT SUMMARY
# ─────────────────────────────────────────────────────────────────────────────
print_summary() {
  local app_url="https://${APP_NAME}.azurewebsites.net"

  section "DEPLOYMENT COMPLETE"

  echo -e "\n  ${BOLD}${GREEN}  Your backend is live on Azure!${NC}\n"

  echo -e "  ${BOLD}${WHITE}Application Endpoints:${NC}"
  label "  Base URL    " "${app_url}"
  label "  Swagger UI  " "${app_url}/swagger-ui.html"
  label "  API Docs    " "${app_url}/api-docs"
  label "  Health      " "${app_url}/actuator/health"
  label "  API Prefix  " "${app_url}/api/v1"

  echo -e "\n  ${BOLD}${WHITE}Azure Resources:${NC}"
  label "  Resource Group " "${RESOURCE_GROUP}"
  label "  App Service    " "${APP_NAME}"
  label "  Service Plan   " "${APP_SERVICE_PLAN} (${SKU})"
  label "  Region         " "${APP_LOCATION:-eastus}"
  label "  Spring Profile " "${SPRING_PROFILE}"

  echo -e "\n  ${BOLD}${WHITE}Useful Commands:${NC}"

  echo -e "  ${DIM}# Stream live application logs${NC}"
  echo -e "  ${CYAN}az webapp log tail --name ${APP_NAME} --resource-group ${RESOURCE_GROUP}${NC}"

  echo -e "\n  ${DIM}# Restart the app${NC}"
  echo -e "  ${CYAN}az webapp restart --name ${APP_NAME} --resource-group ${RESOURCE_GROUP}${NC}"

  echo -e "\n  ${DIM}# View all App Settings${NC}"
  echo -e "  ${CYAN}az webapp config appsettings list --name ${APP_NAME} --resource-group ${RESOURCE_GROUP} -o table${NC}"

  echo -e "\n  ${DIM}# Re-deploy only (after code change, infra already exists)${NC}"
  echo -e "  ${CYAN}./deploy-azure.sh --skip-infra${NC}"

  echo -e "\n  ${DIM}# Delete ALL Azure resources (full cleanup)${NC}"
  echo -e "  ${RED}az group delete --name ${RESOURCE_GROUP} --yes --no-wait${NC}"

  echo ""
  label "  Completed at" "$(date)"
  echo ""
}

# ─────────────────────────────────────────────────────────────────────────────
#  MAIN
# ─────────────────────────────────────────────────────────────────────────────
main() {
  banner
  echo -e "  ${DIM}Started : $(date)${NC}"
  echo -e "  ${DIM}Flags   : skip-build=${SKIP_BUILD}  skip-infra=${SKIP_INFRA}${NC}"

  preflight
  load_secrets

  if [[ "${SKIP_INFRA}" == "false" ]]; then
    create_resource_group
    create_app_service_plan
    create_web_app
    configure_app_settings
    configure_runtime
  else
    warn "Skipping infrastructure setup (--skip-infra). Using existing App Service."
    # Resolve APP_LOCATION for the summary
    APP_LOCATION=$(az appservice plan show \
      --name "${APP_SERVICE_PLAN}" --resource-group "${RESOURCE_GROUP}" \
      --query "location" -o tsv 2>/dev/null || echo "unknown")
  fi

  if [[ "${SKIP_BUILD}" == "false" ]]; then
    build_jar
  else
    warn "Skipping Maven build (--skip-build). Using existing JAR."
  fi

  deploy_jar
  health_check
  print_summary
}

main "$@"
