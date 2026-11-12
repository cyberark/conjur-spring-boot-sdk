#!/usr/bin/env bash

# Publish a pre-release to artifactory

set -euo pipefail

# shellcheck disable=SC2012
target_package="$(ls -1tr target/*.jar |tail -n 1)"

# Copy built jar to ASSET_DIR so it will be attached to the Github Release

if [[ -n "${ASSET_DIR:-}" ]] && [[ -d "${ASSET_DIR:-}" ]]; then
    echo "Copying ${target_package} to Asset Dir: ${ASSET_DIR}"
    cp target/*.jar "${ASSET_DIR}"
else
    echo "ASSET_DIR is unset, unable to copy ${target_package} to ASSET_DIR for github release. ❌"
    exit 1
fi

mkdir -p maven_cache

if [[ "${MODE:-}" == "PROMOTE" ]]; then
    echo "PROMOTE build, publishing to central portal (maven central)"
    maven_profiles="central-portal,sign"

    docker run \
        -e CENTRAL_PORTAL_USERNAME \
        -e CENTRAL_PORTAL_TOKEN \
        -e ARTIFACTORY_HOST \
        -e ARTIFACTORY_USERNAME \
        -e ARTIFACTORY_PASSWORD \
        -e MAVEN_GPG_PASSPHRASE="$(cat "${GPG_PASSWORD}")" \
        --volume "${PWD}:${PWD}" \
        --volume "${PWD}/maven_cache":/root/.m2 \
        --volume "$GPG_PASSWORD:/gpg_password" \
        --volume "$GPG_PRIVATE_KEY:/gpg_key" \
        --workdir "${PWD}" \
        tools \
            /bin/bash -ec "
                           # maven-gpg-plugin 3.x uses GnuPG's agent model. In a headless Docker
                           # environment the agent refuses --pinentry-mode loopback unless
                           # allow-loopback-pinentry is set in gpg-agent.conf. Configure it
                           # first, then kill any existing agent so it reloads with the new config.
                           mkdir -p /root/.gnupg && chmod 700 /root/.gnupg
                           echo 'allow-loopback-pinentry' > /root/.gnupg/gpg-agent.conf
                           gpgconf --kill gpg-agent
                           gpg --batch --passphrase-file /gpg_password --trust-model always --import /gpg_key
                           mvn --batch-mode  --settings mvn-settings.xml --file pom.xml deploy -Dmaven.test.skip -P ${maven_profiles}"
else
    echo "Release build, skipping Maven deploy (handled by pipeline library)"
fi
