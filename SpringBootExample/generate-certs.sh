#!/usr/bin/env bash
# generate-certs.sh — creates a self-signed CA and a client certificate for
# authn-cert E2E testing of the conjur-spring-boot-sdk.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CERTS_DIR="${CERTS_DIR:-"$SCRIPT_DIR/tmp/certs"}"
CERT_CN="${CERT_CN:-spring-cert-host}"

mkdir -p "$CERTS_DIR"
echo "[INFO] Generating PKI in $CERTS_DIR (CN=$CERT_CN) ..."

# ── CA ───────────────────────────────────────────────────────────────────────
echo "[INFO] Generating CA private key and self-signed certificate ..."
openssl genrsa -out "$CERTS_DIR/ca.key" 4096 # gitleaks:allow

openssl req -x509 -new -nodes \
  -key "$CERTS_DIR/ca.key" \
  -sha256 -days 3650 \
  -subj "/CN=spring-sdk-cert-ca" \
  -out "$CERTS_DIR/ca.crt"

echo "[INFO] Generating client private key (PKCS#8) ..."
openssl genpkey -algorithm RSA \
  -pkeyopt rsa_keygen_bits:2048 \
  -out "$CERTS_DIR/client.key" # gitleaks:allow

echo "[INFO] Generating client certificate (CN=$CERT_CN) ..."
openssl req -new \
  -key "$CERTS_DIR/client.key" \
  -subj "/CN=$CERT_CN" \
  -out "$CERTS_DIR/client.csr"

openssl x509 -req \
  -in "$CERTS_DIR/client.csr" \
  -CA "$CERTS_DIR/ca.crt" \
  -CAkey "$CERTS_DIR/ca.key" \
  -CAcreateserial \
  -out "$CERTS_DIR/client.crt" \
  -days 365 -sha256

rm -f "$CERTS_DIR/client.csr" "$CERTS_DIR/ca.srl"

echo "[DONE] Certificates generated:"
ls -la "$CERTS_DIR/"
