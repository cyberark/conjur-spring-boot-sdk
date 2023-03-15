#!/bin/bash

set -euo pipefail
set -x

mkdir -p maven_cache

# Find the plugin jar
# Array variable to force glob expansion during assignment
jar_paths=(../target/*$(<../VERSION).jar)

# Get first item from the array
jar_path="${jar_paths[0]}"

# Extract jar filename from path
jar="$(basename "${jar_path}")"

# Bring the spring boot conjur jar into this directory so
# it is accessible to docker.
cp "${jar_path}" "${jar}"

docker run \
    --volume "${PWD}:${PWD}" \
    --volume "${PWD}/maven_cache":/root/.m2 \
    --workdir "${PWD}" \
    tools \
        mvn --batch-mode install:install-file \
        -Dfile="${jar}" \
        -DgroupId=com.cyberark \
        -DartifactId=conjur-sdk-springboot \
        -Dversion="$(<../VERSION)" \
        -Dpackaging=jar \
        -DgeneratePom=true

# Update the sample app pom to use the version of conjur spring boot plugin thats under test
docker run \
    --volume "${PWD}:${PWD}" \
    --volume "${PWD}/maven_cache":/root/.m2 \
    --workdir "${PWD}" \
    tools \
        mvn --batch-mode -f pom.xml versions:use-dep-version -Dincludes=com.cyberark:conjur-sdk-springboot -DdepVersion="$(<../VERSION)"

docker run \
    --volume "${PWD}:${PWD}" \
    --volume "${PWD}/maven_cache":/root/.m2 \
    --workdir "${PWD}" \
    tools \
        mvn --batch-mode -f pom.xml dependency:tree

# Use Tools image to package code
docker run \
    --volume "${PWD}:${PWD}" \
    --volume "${PWD}/maven_cache":/root/.m2 \
    --workdir "${PWD}" \
    tools \
        mvn --batch-mode -f pom.xml clean compile package

docker build \
    --no-cache \
    --build-arg maven_version=3.8.4 \
    --build-arg java_version=11 \
    -t sampleapp \
    .
