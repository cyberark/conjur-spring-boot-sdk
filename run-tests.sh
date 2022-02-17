#!/usr/bin/env bash

set -euo pipefail

mkdir -p maven_cache
docker run \
    --volume "${PWD}:${PWD}" \
    --volume "maven_cache":/root/.m2 \
    --workdir "${PWD}" \
    tools \
        mvn -f pom.xml test
