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

# Use Tools image to package code
if [[ ! -f mvn-settings.xml ]]; then
    echo "Please run summon -e artifactory ./generate-maven-settings.sh before publish.sh"
    exit 1
fi
mkdir -p maven_cache
docker run \
    --volume "${PWD}:${PWD}" \
    --volume "${PWD}/maven_cache":/root/.m2 \
    --workdir "${PWD}" \
    tools \
        cp mvn-settings.xml maven_cache/settings.xml
docker run \
    --volume "${PWD}:${PWD}" \
    --volume "${PWD}/maven_cache":/root/.m2 \
    --workdir "${PWD}" \
    tools \
        mvn -f pom.xml deploy -Dmaven.test.skip
