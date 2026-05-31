#!/usr/bin/env bash
# =============================================================================
#  Curriculum Tracking System — End-to-End Azure App Service Deployment
# =============================================================================
#  What this script does (in order):
#    1.  Pre-flight checks  (az CLI, Java, Maven, Azure login, keys.properties)
#    2.  Load all secrets from keys.properties
#    3.  Create Resource Group
#    4.  Create Azure Key Vault + store every secret
#    5.  Create App Service Plan (Linux, B1)
#    6.  Create Java 17 Web App
#    7.  Enable System-Assigned Managed Identity
#    8.  Grant the identity "get/list" access to Key Vault
#    9.  Configure App Settings wired to Key Vault references
#    10. Set Java startup command
#    11. Enable filesystem logging
#    12. Maven build (clean package -DskipTests)
#    13. Deploy JAR via Azure CLI
#    14. Health-check poll with retry
#    15. Print deployment summary + useful commands
#
#  Usage:
#    ./deploy-azure.sh                 — full end-to-end deploy
#    ./deploy-azure.sh --skip-build    — skip Maven build, deploy existing JAR
#    ./deploy-azure.sh --skip-infra    — skip infra, only build + deploy JAR
#    ./deploy-azure.sh --skip-secrets  — skip Key Vault secret writes (re-use existing)
#    ./deploy-azure.sh --skip-build --skip-infra   — only (re-)deploy the JAR
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
NC='\033[0m'   # reset

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

info()    { echo -e "    ${CYAN}→${NC}  $1"; }
ok()      { echo -e "    ${GREEN}✔${NC}  $1"; }
warn()    { echo -e "    ${YELLOW}⚠${NC}  $1"; }
skip()    { echo -e "    ${DIM}↷  $1 — already exists, skipping${NC}"; }
die()     { echo -e "\n    ${RED}✖  FATAL: $1${NC}\n"; exit 1; }
kv_ref()  { echo -e "    ${DIM}↳  KV ref: $1${NC}"; }
label()   {
  local k="$1"; local v="$2"
  printf "    ${WHITE}${BOLD}%-22s${NC}${CYAN}%s${NC}\n" "${k}:" "${v}"
}

# ─────────────────────────────────────────────────────────────────────────────
#  ╔══════════════════════════════════╗
#  ║   CONFIGURATION — edit here     ║
#  ╚══════════════════════════════════╝
# ─────────────────────────────────────────────────────────────────────────────

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KEYS_FILE="${SCRIPT_DIR}/keys.properties"

# ── Azure resource names ──────────────────────────────────────────────────────
RESOURCE_GROUP="curriculum-tracking-rg"
LOCATION="eastus"

# App Service (Web App name must be globally unique → becomes <name>.azurewebsites.net)
APP_NAME="curriculum-tracking-api"
APP_SERVICE_PLAN="curriculum-plan"
SKU="B1"          # B1 ≈ $13/mo on student subscription. Change to F1 for free (cold-start caveat).

# Key Vault (3-24 chars, globally unique, alphanumeric + hyphens only)
KEY_VAULT_BASE="curriculum-kv"

# Spring profile that maps to application-azure.yml
SPRING_PROFILE="azure"

# Path to built JAR (resolved after build)
JAR_GLOB="${SCRIPT_DIR}/target/curriculum-tracking-system-*.jar"

# ── Flags ─────────────────────────────────────────────────────────────────────
SKIP_BUILD=false
SKIP_INFRA=false
SKIP_SECRETS=false

for arg in "$@"; do
  case $arg in
    --skip-build)   SKIP_BUILD=true   ;;
    --skip-infra)   SKIP_INFRA=true   ;;
    --skip-secrets) SKIP_SECRETS=true ;;
  esac
done

# ─────────────────────────────────────────────────────────────────────────────
#  UTILITIES
# ─────────────────────────────────────────────────────────────────────────────

# Read a value from keys.properties (ignores comment lines)
prop() {
  grep -E "^${1}=" "${KEYS_FILE}" | head -1 | cut -d'=' -f2-
}

# Check whether an az command exits 0 (resource exists)
az_exists() { "$@" &>/dev/null && return 0 || return 1; }

# ─────────────────────────────────────────────────────────────────────────────
#  STEP 0 — PRE-FLIGHT CHECKS
# ─────────────────────────────────────────────────────────────────────────────
preflight() {
  section "PRE-FLIGHT CHECKS"

  step "Verifying required tools"

  command -v az   &>/dev/null || die "Azure CLI not found. Install: https://aka.ms/installazurecli"
  ok "az    $(az version --query '"azure-cli"' -o tsv)"

  command -v mvn  &>/dev/null || die "Maven not found. Install Maven 3.8+ and retry."
  ok "mvn   $(mvn --version 2>&1 | head -1 | sed 's/Apache Maven //')"

  command -v java &>/dev/null || die "Java not found. Install Java 17+."
  ok "java  $(java -version 2>&1 | head -1)"

  command -v curl &>/dev/null || die "curl not found (needed for health check)."
  ok "curl  $(curl --version | head -1)"

  step "Verifying Azure login"
  local acct
  acct=$(az account show --query "name" -o tsv 2>/dev/null) \
    || die "Not logged in. Run: az login"
  local sub_id
  sub_id=$(az account show --query "id" -o tsv)
  ok "Authenticated"
  label "Subscription" "${acct}"
  label "Subscription ID" "${sub_id}"

  step "Verifying keys.properties"
  [[ -f "${KEYS_FILE}" ]] || die "keys.properties not found at: ${KEYS_FILE}"
  ok "keys.properties found"
}

# ─────────────────────────────────────────────────────────────────────────────
#  LOAD SECRETS
# ─────────────────────────────────────────────────────────────────────────────
load_secrets() {
  section "LOADING SECRETS FROM keys.properties"
  step "Parsing keys.properties"

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

  # Validate the critical ones
  [[ -n "${DB_URL}" ]]          || die "db.url missing from keys.properties"
  [[ -n "${DB_PASSWORD}" ]]     || die "db.password missing from keys.properties"
  [[ -n "${JWT_SECRET}" ]]      || die "jwt.secret missing from keys.properties"
  [[ -n "${REDIS_HOST}" ]]      || die "redis.host missing from keys.properties"
  [[ -n "${REDIS_PASSWORD}" ]]  || die "redis.password missing from keys.properties"

  ok "All 20 secrets loaded"
  label "DB host" "$(echo "${DB_URL}" | sed 's/jdbc:postgresql:\/\///' | cut -d'/' -f1)"
  label "Redis host" "${REDIS_HOST}:${REDIS_PORT}"
  label "AWS region" "${AWS_REGION} / bucket: ${AWS_S3_BUCKET}"
}

# ─────────────────────────────────────────────────────────────────────────────
#  STEP 1 — RESOURCE GROUP
# ─────────────────────────────────────────────────────────────────────────────
create_resource_group() {
  section "AZURE INFRASTRUCTURE"
  step "Resource Group: ${RESOURCE_GROUP}"

  if az_exists az group show --name "${RESOURCE_GROUP}"; then
    skip "Resource group '${RESOURCE_GROUP}'"
  else
    az group create \
      --name "${RESOURCE_GROUP}" \
      --location "${LOCATION}" \
      --output none
    ok "Resource group '${RESOURCE_GROUP}' created in ${LOCATION}"
  fi
}

# ─────────────────────────────────────────────────────────────────────────────
#  STEP 2 — KEY VAULT (unique name resolution)
# ─────────────────────────────────────────────────────────────────────────────
create_key_vault() {
  step "Key Vault"

  # Derive a unique suffix from subscription id (first 8 hex chars)
  local suffix
  suffix=$(az account show --query "id" -o tsv | tr -d '-' | cut -c1-8)
  KEY_VAULT_NAME="${KEY_VAULT_BASE}-${suffix}"

  info "Resolved Key Vault name: ${KEY_VAULT_NAME}"

  if az_exists az keyvault show --name "${KEY_VAULT_NAME}" --resource-group "${RESOURCE_GROUP}"; then
    skip "Key Vault '${KEY_VAULT_NAME}'"
  else
    az keyvault create \
      --name "${KEY_VAULT_NAME}" \
      --resource-group "${RESOURCE_GROUP}" \
      --location "${LOCATION}" \
      --enable-rbac-authorization false \
      --sku standard \
      --output none
    ok "Key Vault '${KEY_VAULT_NAME}' created"
  fi
  label "Key Vault URI" "https://${KEY_VAULT_NAME}.vault.azure.net"
}

# ─────────────────────────────────────────────────────────────────────────────
#  STEP 3 — STORE SECRETS IN KEY VAULT
# ─────────────────────────────────────────────────────────────────────────────
store_secrets() {
  step "Granting current user access to Key Vault for secret writes"

  # Try signed-in user UPN first; fall back to object-id for service principals
  local user_upn
  user_upn=$(az account show --query "user.name" -o tsv 2>/dev/null || true)

  if [[ -n "${user_upn}" && "${user_upn}" != "null" ]]; then
    az keyvault set-policy \
      --name "${KEY_VAULT_NAME}" \
      --upn "${user_upn}" \
      --secret-permissions get list set delete \
      --output none
    ok "Access policy set for user: ${user_upn}"
  else
    local oid
    oid=$(az ad signed-in-user show --query "id" -o tsv 2>/dev/null) \
      || die "Cannot determine current user identity for Key Vault policy."
    az keyvault set-policy \
      --name "${KEY_VAULT_NAME}" \
      --object-id "${oid}" \
      --secret-permissions get list set delete \
      --output none
    ok "Access policy set for object-id: ${oid}"
  fi

  step "Storing secrets in Key Vault (20 secrets)"

  _kv_set() {
    az keyvault secret set \
      --vault-name "${KEY_VAULT_NAME}" \
      --name "$1" \
      --value "$2" \
      --output none
    ok "Secret: $1"
  }

  # ── Database ──────────────────────────────────────────────────────────────
  _kv_set "db-url"                    "${DB_URL}"
  _kv_set "db-username"               "${DB_USERNAME}"
  _kv_set "db-password"               "${DB_PASSWORD}"

  # ── Mail ──────────────────────────────────────────────────────────────────
  _kv_set "mail-username"             "${MAIL_USERNAME}"
  _kv_set "mail-password"             "${MAIL_PASSWORD}"

  # ── JWT ───────────────────────────────────────────────────────────────────
  _kv_set "jwt-secret"                "${JWT_SECRET}"
  _kv_set "jwt-expiration-ms"         "${JWT_EXPIRATION_MS}"
  _kv_set "jwt-refresh-expiration-ms" "${JWT_REFRESH_EXPIRATION_MS}"

  # ── Admin ─────────────────────────────────────────────────────────────────
  _kv_set "admin-username"            "${ADMIN_USERNAME}"
  _kv_set "admin-email"               "${ADMIN_EMAIL}"
  _kv_set "admin-password"            "${ADMIN_PASSWORD}"

  # ── Redis ─────────────────────────────────────────────────────────────────
  _kv_set "redis-host"                "${REDIS_HOST}"
  _kv_set "redis-port"                "${REDIS_PORT}"
  _kv_set "redis-password"            "${REDIS_PASSWORD}"
  _kv_set "redis-database"            "${REDIS_DATABASE}"
  _kv_set "redis-ssl-enabled"         "${REDIS_SSL_ENABLED}"

  # ── AWS / S3 ──────────────────────────────────────────────────────────────
  _kv_set "aws-access-key-id"         "${AWS_ACCESS_KEY_ID}"
  _kv_set "aws-secret-access-key"     "${AWS_SECRET_ACCESS_KEY}"
  _kv_set "aws-s3-bucket"             "${AWS_S3_BUCKET}"
  _kv_set "aws-region"                "${AWS_REGION}"

  ok "All 20 secrets stored in Key Vault"
}

# ─────────────────────────────────────────────────────────────────────────────
#  STEP 4 — APP SERVICE PLAN
# ─────────────────────────────────────────────────────────────────────────────
create_app_service_plan() {
  step "App Service Plan: ${APP_SERVICE_PLAN} (${SKU}, Linux)"

  if az_exists az appservice plan show --name "${APP_SERVICE_PLAN}" --resource-group "${RESOURCE_GROUP}"; then
    skip "App Service Plan '${APP_SERVICE_PLAN}'"
  else
    az appservice plan create \
      --name "${APP_SERVICE_PLAN}" \
      --resource-group "${RESOURCE_GROUP}" \
      --location "${LOCATION}" \
      --sku "${SKU}" \
      --is-linux \
      --output none
    ok "App Service Plan created (Linux ${SKU})"
  fi
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
    ok "Web App '${APP_NAME}' created with Java 17 runtime"
  fi
  label "App URL" "https://${APP_NAME}.azurewebsites.net"
}

# ─────────────────────────────────────────────────────────────────────────────
#  STEP 6 — MANAGED IDENTITY + KEY VAULT ACCESS
# ─────────────────────────────────────────────────────────────────────────────
enable_managed_identity() {
  step "Enabling System-Assigned Managed Identity on Web App"

  local principal_id
  principal_id=$(az webapp identity assign \
    --name "${APP_NAME}" \
    --resource-group "${RESOURCE_GROUP}" \
    --query "principalId" \
    --output tsv)

  ok "Managed Identity enabled"
  label "Principal ID" "${principal_id}"

  step "Granting Managed Identity 'get/list' access on Key Vault"
  info "Waiting 20 s for Azure AD identity propagation..."
  sleep 20

  az keyvault set-policy \
    --name "${KEY_VAULT_NAME}" \
    --object-id "${principal_id}" \
    --secret-permissions get list \
    --output none

  ok "Key Vault access policy applied for App Service identity"
}

# ─────────────────────────────────────────────────────────────────────────────
#  STEP 7 — APP SETTINGS (Key Vault references + static config)
# ─────────────────────────────────────────────────────────────────────────────
configure_app_settings() {
  step "Configuring App Settings with Key Vault references"

  # Convenience alias for the Key Vault reference syntax
  kv() { echo "@Microsoft.KeyVault(VaultName=${KEY_VAULT_NAME};SecretName=$1)"; }

  info "Mapping 20 secrets to Key Vault references..."

  az webapp config appsettings set \
    --name "${APP_NAME}" \
    --resource-group "${RESOURCE_GROUP}" \
    --output none \
    --settings \
      SPRING_PROFILES_ACTIVE="${SPRING_PROFILE}" \
      WEBSITES_PORT="80" \
      SCM_DO_BUILD_DURING_DEPLOYMENT="false" \
      \
      "DB_URL=$(kv db-url)" \
      "DB_USERNAME=$(kv db-username)" \
      "DB_PASSWORD=$(kv db-password)" \
      \
      "MAIL_USERNAME=$(kv mail-username)" \
      "MAIL_PASSWORD=$(kv mail-password)" \
      \
      "JWT_SECRET=$(kv jwt-secret)" \
      "JWT_EXPIRATION_MS=$(kv jwt-expiration-ms)" \
      "JWT_REFRESH_EXPIRATION_MS=$(kv jwt-refresh-expiration-ms)" \
      \
      "ADMIN_USERNAME=$(kv admin-username)" \
      "ADMIN_EMAIL=$(kv admin-email)" \
      "ADMIN_PASSWORD=$(kv admin-password)" \
      \
      "REDIS_HOST=$(kv redis-host)" \
      "REDIS_PORT=$(kv redis-port)" \
      "REDIS_PASSWORD=$(kv redis-password)" \
      "REDIS_DATABASE=$(kv redis-database)" \
      "REDIS_SSL_ENABLED=$(kv redis-ssl-enabled)" \
      \
      "AWS_ACCESS_KEY_ID=$(kv aws-access-key-id)" \
      "AWS_SECRET_ACCESS_KEY=$(kv aws-secret-access-key)" \
      "AWS_S3_BUCKET=$(kv aws-s3-bucket)" \
      "AWS_REGION=$(kv aws-region)"

  ok "App Settings configured — all secrets load from Key Vault at runtime"
}

# ─────────────────────────────────────────────────────────────────────────────
#  STEP 8 — STARTUP COMMAND + LOGGING
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
  ok "Logging enabled (filesystem)"
}

# ─────────────────────────────────────────────────────────────────────────────
#  STEP 9 — MAVEN BUILD
# ─────────────────────────────────────────────────────────────────────────────
build_jar() {
  section "BUILD"
  step "Maven clean package (tests skipped for deployment speed)"
  info "Run 'mvn test' separately to verify the test suite."

  cd "${SCRIPT_DIR}"
  mvn clean package -DskipTests --no-transfer-progress

  # Resolve the JAR path
  JAR_PATH=$(ls ${JAR_GLOB} 2>/dev/null | grep -v sources | head -1)
  [[ -z "${JAR_PATH}" ]] && die "JAR not found after build. Check Maven output above."

  local size
  size=$(du -sh "${JAR_PATH}" | cut -f1)
  ok "Build complete: $(basename "${JAR_PATH}") (${size})"
}

# ─────────────────────────────────────────────────────────────────────────────
#  STEP 10 — DEPLOY JAR
# ─────────────────────────────────────────────────────────────────────────────
deploy_jar() {
  section "DEPLOYMENT"
  step "Deploying JAR to Azure App Service"

  # Resolve JAR if we skipped build
  if [[ -z "${JAR_PATH:-}" ]]; then
    JAR_PATH=$(ls ${JAR_GLOB} 2>/dev/null | grep -v sources | head -1)
    [[ -z "${JAR_PATH}" ]] && die "No JAR found at ${JAR_GLOB}. Run without --skip-build."
  fi

  local size
  size=$(du -sh "${JAR_PATH}" | cut -f1)
  label "Uploading" "$(basename "${JAR_PATH}") (${size})"
  label "Destination" "https://${APP_NAME}.azurewebsites.net"

  az webapp deploy \
    --name "${APP_NAME}" \
    --resource-group "${RESOURCE_GROUP}" \
    --src-path "${JAR_PATH}" \
    --type jar \
    --async false \
    --output none

  ok "JAR deployed successfully"
  info "Azure App Service is now starting the application..."
}

# ─────────────────────────────────────────────────────────────────────────────
#  STEP 11 — HEALTH CHECK POLL
# ─────────────────────────────────────────────────────────────────────────────
health_check() {
  section "HEALTH CHECK"
  step "Polling application health"

  local base_url="https://${APP_NAME}.azurewebsites.net"
  local health_url="${base_url}/actuator/health"
  local max=24          # 24 × 15 s = 6 minutes total
  local delay=15
  local attempt=0

  label "Health URL" "${health_url}"
  info "Polling every ${delay}s (up to $((max * delay / 60)) min)..."
  echo ""

  while [[ ${attempt} -lt ${max} ]]; do
    attempt=$((attempt + 1))
    local code
    code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 10 "${health_url}" 2>/dev/null || echo "000")

    printf "    ${DIM}[%02d/%02d]${NC} HTTP %s" "${attempt}" "${max}" "${code}"

    case "${code}" in
      200)
        echo -e "  ${GREEN}← UP ✔${NC}"
        ok "Application is healthy!"
        return 0
        ;;
      401|403)
        echo -e "  ${YELLOW}← secured (app is running) ✔${NC}"
        ok "Application is running (health endpoint requires auth)"
        return 0
        ;;
      000)
        echo -e "  ${DIM}← not yet reachable${NC}"
        ;;
      *)
        echo -e "  ${DIM}← starting...${NC}"
        ;;
    esac
    sleep "${delay}"
  done

  warn "App did not respond within timeout — it may still be warming up."
  warn "Stream logs: az webapp log tail --name ${APP_NAME} --resource-group ${RESOURCE_GROUP}"
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
  label "  Key Vault      " "${KEY_VAULT_NAME}"
  label "  Region         " "${LOCATION}"
  label "  Spring Profile " "${SPRING_PROFILE}"

  echo -e "\n  ${BOLD}${WHITE}Useful Commands:${NC}"

  echo -e "  ${DIM}# Stream live application logs${NC}"
  echo -e "  ${CYAN}az webapp log tail --name ${APP_NAME} --resource-group ${RESOURCE_GROUP}${NC}"

  echo -e "\n  ${DIM}# Restart the app${NC}"
  echo -e "  ${CYAN}az webapp restart --name ${APP_NAME} --resource-group ${RESOURCE_GROUP}${NC}"

  echo -e "\n  ${DIM}# View all App Settings${NC}"
  echo -e "  ${CYAN}az webapp config appsettings list --name ${APP_NAME} --resource-group ${RESOURCE_GROUP} -o table${NC}"

  echo -e "\n  ${DIM}# List Key Vault secrets${NC}"
  echo -e "  ${CYAN}az keyvault secret list --vault-name ${KEY_VAULT_NAME} -o table${NC}"

  echo -e "\n  ${DIM}# Re-deploy only (after code change)${NC}"
  echo -e "  ${CYAN}./deploy-azure.sh --skip-infra --skip-secrets${NC}"

  echo -e "\n  ${DIM}# Clean up ALL resources (deletes everything)${NC}"
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
  echo -e "  ${DIM}Started: $(date)${NC}"
  echo -e "  ${DIM}Flags  : skip-build=${SKIP_BUILD}  skip-infra=${SKIP_INFRA}  skip-secrets=${SKIP_SECRETS}${NC}"

  preflight
  load_secrets

  if [[ "${SKIP_INFRA}" == "false" ]]; then
    create_resource_group
    create_key_vault

    if [[ "${SKIP_SECRETS}" == "false" ]]; then
      store_secrets
    else
      warn "Skipping secret writes (--skip-secrets). Key Vault must already be populated."
    fi

    create_app_service_plan
    create_web_app
    enable_managed_identity
    configure_app_settings
    configure_runtime
  else
    warn "Skipping infrastructure setup (--skip-infra)."
    # Still need KEY_VAULT_NAME for summary — derive it
    local suffix
    suffix=$(az account show --query "id" -o tsv | tr -d '-' | cut -c1-8)
    KEY_VAULT_NAME="${KEY_VAULT_BASE}-${suffix}"
    info "Using existing resources. App: ${APP_NAME}, KV: ${KEY_VAULT_NAME}"
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
