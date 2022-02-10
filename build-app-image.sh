#!/bin/bash

set -euo pipefail

if ! find target/*.jar; then
    echo "No jar found in target directory, please run build-package.sh before build-app-image.sh"
    exit 1
fi

if [[ ! -r VERSION ]]; then
  echo "VERSION file not present, please run build-package.sh before build-app-image.sh"
  exit 1
fi

docker build \
    --build-arg maven_version=3.8.4 \
    --build-arg java_version=11 \
    -t conjur-spring-boot-plugin \
    -t "registry.tld/conjur-spring-boot-plugin:$(<VERSION)" \
    .

if [[ -n "${BUILD_NUMBER:-}" ]]; then
  docker push  "registry.tld/conjur-spring-boot-plugin:$(<VERSION)"
fi
