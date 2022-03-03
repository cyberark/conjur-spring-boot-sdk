#!/usr/bin/env bash

set -euo pipefail

mkdir -p maven_cache
docker run \
    --volume "${PWD}:${PWD}" \
    --volume "${PWD}/maven_cache":/root/.m2 \
    --workdir "${PWD}" \
    tools \
        mvn -f pom.xml jacoco:prepare-agent test jacoco:report

cp target/site/jacoco/jacoco.xml src/main/java
cp target/site/jacoco/index.html jacoco.html
