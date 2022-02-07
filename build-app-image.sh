#!/bin/bash

set -euo pipefail

if [[ -n "${BUILD_NUMBER:-}" ]]; then
  tag="$(<VERSION)"
else
  tag="development"
fi

find target/*.jar || {
    echo "No jar found in target directory, please run build-package.sh before build-app-image.sh"
    exit 1
}

# Build tools image
#FROM maven:${maven_version}-openjdk-${java_version}-slim
docker build \
    --build-arg maven_version=3.8.4 \
    --build-arg java_version=11 \
    -t conjur-spring-boot-plugin \
    -t "registry.tld/conjur-spring-boot-plugin:${tag}" \
    .


#docker tag conjur-spring-boot-plugin registry.tld/conjur-spring-boot-plugin
if [[ -n "${BUILD_NUMBER:-}" ]]; then
  docker push  "registry.tld/conjur-spring-boot-plugin:${tag}" 
fi