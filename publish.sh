#!/usr/bin/env bash

set -euo pipefail

echo "Insert code to push to Artifactory"

# Copy built jar to ASSET_DIR so it will be attached to the Github Release
if [[ -d "${ASSET_DIR}" ]]; then
    cp target/*.jar "${ASSET_DIR}"
fi
