#!/bin/bash

set -euo pipefail

docker build . --build-arg java_version=11
