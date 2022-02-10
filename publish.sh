#!/usr/bin/env bash

set -euo pipefail

# Load docker_rt function from utils.sh
# shellcheck source=/dev/null
. "$(dirname "${0}")/utils.sh"

# shellcheck disable=SC2012
target_package="$(ls -1tr target/*.jar |tail -n 1)"

# Copy built jar to ASSET_DIR so it will be attached to the Github Release

if [[ -n "${ASSET_DIR:-}" ]] && [[ -d "${ASSET_DIR:-}" ]]; then
    echo "Copying ${target_package} to Asset Dir: ${ASSET_DIR}"
    cp target/*.jar "${ASSET_DIR}"
else
    echo "ASSET_DIR is unset, unable to copy ${target_package} to ASSET_DIR for github release. ❌"
    exit 1
fi

if [[ -n "${JFROG_REPO:-}" ]]; then
    echo "Uploading ${target_package} to Artifcatory: ${JFROG_REPO}"
    docker_rt upload --flat "${target_package}" "${JFROG_REPO}/"
else
    echo "JFROG_REPO is unset, unable to upload ${target_package} to Artifactory. ❌"
    exit 1
fi

# Push image to internal registry when running in Jenkins, but not when running locally.
if [[ -r VERSION ]] && [[ -n "${BUILD_NUMBER}" ]]; then
    #push the image to internal registry
    docker push "registry.tld/conjur-spring-boot-plugin:$(<VERSION)"
fi
