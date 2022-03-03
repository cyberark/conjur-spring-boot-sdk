#!/bin/bash

set -euo pipefail
set -x

mkdir -p maven_cache

cp ../target/*.jar spring-boot-conjur.jar
docker run \
    --volume "${PWD}:${PWD}" \
    --volume "${PWD}/maven_cache":/root/.m2 \
    --workdir "${PWD}" \
    tools \
        mvn install:install-file \
        -Dfile=spring-boot-conjur.jar \
        -DgroupId=com.cyberark.conjur.springboot \
        -DartifactId=Spring-boot-conjur \
        -Dversion="$(<../VERSION)" \
        -Dpackaging=jar \
        -DgeneratePom=true

# Update the sample app pom to use the version of conjur spring boot plugin thats under test
docker run \
    --volume "${PWD}:${PWD}" \
    --volume "${PWD}/maven_cache":/root/.m2 \
    --workdir "${PWD}" \
    tools \
        mvn -f pom.xml versions:use-dep-version -Dincludes=com.cyberark.conjur.springboot:Spring-boot-conjur -DdepVersion="$(<../VERSION)"

# Use Tools image to package code
docker run \
    --volume "${PWD}:${PWD}" \
    --volume "${PWD}/maven_cache":/root/.m2 \
    --workdir "${PWD}" \
    tools \
        mvn -f pom.xml clean compile package

docker build \
    --no-cache \
    --build-arg maven_version=3.8.4 \
    --build-arg java_version=11 \
    -t sampleapp \
    .
