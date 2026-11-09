#!/bin/bash

# Publish a Release to Artifactory and Maven Central

set -euo pipefail

mkdir -p maven_cache

# Jenkins reads the changelog and writes the latest version to a file called VERSION
# If a developer runs these scripts locally, the VERSION file won't exist, so
# we generate a placeholder VERSION file.
if [[ ! -r VERSION ]]; then
    echo "0.0.1-SNAPSHOT" > VERSION
fi

# Use tools image to update version in pom file to match the version in CHANGELOG.md
docker run \
    -e ARTIFACTORY_HOST \
    -e ARTIFACTORY_USERNAME \
    -e ARTIFACTORY_PASSWORD \
    --volume "${PWD}:${PWD}" \
    --volume "${PWD}/maven_cache":/root/.m2 \
    --workdir "${PWD}" \
    tools \
        mvn --batch-mode --settings mvn-settings.xml versions:set -DnewVersion="$(<VERSION)" -Dmaven.test.skip
# TODO: Update sample app dependency to use this version.

# Use Tools image to package code
docker run \
    -e ARTIFACTORY_HOST \
    -e ARTIFACTORY_USERNAME \
    -e ARTIFACTORY_PASSWORD \
    --volume "${PWD}:${PWD}" \
    --volume "${PWD}/maven_cache":/root/.m2 \
    --workdir "${PWD}" \
    tools \
        mvn --batch-mode --settings mvn-settings.xml -f pom.xml clean package -Dmaven.test.skip
