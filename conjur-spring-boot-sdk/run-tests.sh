#!/usr/bin/env bash

echo "Tests are currently run from SpringBootExample/start"

 #set -euo pipefail

 #. SpringBootExample/.env

 #echo "${CONJUR_AUTHN_API_KEY}" > api_key

 #export CONJUR_AUTHN_TOKEN_FILE="${PWD}/api_key"

 #mkdir -p maven_cache
 #docker run \
 #    --volume "${PWD}:${PWD}" \
 #    --volume "${PWD}/maven_cache":/root/.m2 \
 #    --volume "${CONJUR_AUTHN_TOKEN_FILE}:${CONJUR_AUTHN_TOKEN_FILE}" \
 #    --volume "${PWD}/conjur-dev.pem:/conjur-dev.pem" \
 #    --network springbootexample_default \
 #    -e CONJUR_AUTHN_TOKEN_FILE \
 #    -e CONJUR_ACCOUNT \
 #    -e CONJUR_AUTHN_LOGIN \
 #    -e CONJUR_APPLIANCE_URL \
 #    -e CONJUR_AUTHN_API_KEY \
 #    -e CONJUR_CERT_FILE="/conjur-dev.pem" \
 #    --workdir "${PWD}" \
 #    tools \
 #        mvn -f pom.xml jacoco:prepare-agent test jacoco:report


#docker run \
#  --volume "$(git rev-parse --show-toplevel):/repo" \
#  --volume "${PWD}/maven_cache":/root/.m2 \
#  --volume "${PWD}/api_key:/api_key" \
#  --volume "${PWD}/conjur-dev.pem:/conjur-dev.pem" \
#  --workdir "/repo" \
#  --rm \
#  --entrypoint /bin/bash \
#app \
#-ec 'mvn -f pom.xml jacoco:prepare-agent test jacoco:report'

 #cp target/site/jacoco/jacoco.xml src/main/java
 #cp target/site/jacoco/index.html jacoco.html
