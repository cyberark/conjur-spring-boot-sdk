#!/bin/bash

set -euo pipefail

docker build . --build-arg maven_version=3.8.4 --build-arg java_version=11
#FROM maven:${maven_version}-openjdk-${java_version}-slim