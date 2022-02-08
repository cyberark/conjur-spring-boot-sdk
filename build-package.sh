#!/bin/bash

set -euo pipefail

mkdir -p maven_cache

# Create version file for local development (this is not used when running in Jenkins)
if [[ ! -r VERSION ]]; then
    echo "0.0.0-dev" > VERSION
fi

# Use tools image to update version in pom file to match the version in CHANGELOG.md
docker run \
    --volume "${PWD}:${PWD}" \
    --volume "maven_cache":/root/.m2 \
    --workdir "${PWD}" \
    tools \
        mvn versions:set -DnewVersion="$(<VERSION)"

# Use Tools image to package code
docker run \
    --volume "${PWD}:${PWD}" \
    --volume "maven_cache":/root/.m2 \
    --workdir "${PWD}" \
    tools \
        mvn -f pom.xml package
