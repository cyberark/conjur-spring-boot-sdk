#!/usr/bin/env bash

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
