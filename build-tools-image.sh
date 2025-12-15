#!/bin/bash

set -euo pipefail

# Build tools image
#FROM maven:${maven_version}-openjdk-${java_version}-slim
docker build --no-cache \
    --build-arg maven_version=3.8.4 \
    --build-arg java_version=17 \
    -t tools \
    -f tools.Dockerfile \
    .
