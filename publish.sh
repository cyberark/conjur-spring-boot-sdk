#!/usr/bin/env bash

set -euo pipefail

# Run the artifactory subcommand of the jfrog cli within docker
docker_rt(){
    action="${1}"
    shift
    docker run \
        --rm \
        --volume "$(pwd)":"$(pwd)" \
        --workdir "$(pwd)" \
        --env JFROG_CLI_OFFER_CONFIG \
        --env CI="true" \
        docker.bintray.io/jfrog/jfrog-cli-go:latest \
        jfrog rt "${action}" \
            --url "${JFROG_URL}" \
            --user "${JFROG_USERNAME}" \
            --apikey "${JFROG_APIKEY}" \
            "${@}"
}

target_package="$(ls -1tr target/*.jar |tail -n 1)"

# Copy built jar to ASSET_DIR so it will be attached to the Github Release

if [[ -n "${ASSET_DIR:-}" ]] && [[ -d "${ASSET_DIR:-}" ]]; then
    echo "Copying ${target_package} to Asset Dir: ${ASSET_DIR}"
    cp target/*.jar "${ASSET_DIR}"
else
    echo "ASSET_DIR is unset, unable to copy ${target_package} to ASSET_DIR for github release. ❌"
    exit 1
fi

if [[ -n "${JFROG_REPO:-}" ]]; then
    echo "Uploading ${target_package} to Artifcatory: ${JFROG_REPO}"
    docker_rt upload --flat "${target_package}" "${JFROG_REPO}/"
else
    echo "JFROG_REPO is unset, unable to upload ${target_package} to Artifactory. ❌"
    exit 1
fi


#Create docker image for conjur-spring-boot-plugin
#docker tag conjur-spring-boot-plugin registry.tld/conjur-spring-boot-plugin

#push the image to internal registry
#docker push registry.tld/conjur-spring-boot-plugin


