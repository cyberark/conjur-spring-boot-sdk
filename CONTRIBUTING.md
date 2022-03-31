# Contributing

[Are you accepting contributions at this time? If not, please state that here.
No need to include content from the rest of this document.]

For general contribution and community guidelines, please see the [community repo](https://github.com/cyberark/community).

## Table of Contents

- [Development](#development)
- [Testing](#testing)
- [Releases](#releases)
- [Contributing](#contributing-workflow)

## Development

TODO:
[What development tools are required to start working on this project?]

## Testing

1) Go into SpringBootExample folder and set up sample client application for connecting to Conjur via Spring-boot-sdk

    $ cd SpringBootExample

2) Create Docker image for sample client application.

    $ ./build-sampleapp-image.sh

3) Set up Sample client app and Conjur OSS environment as docker containers to test Spring-boot-sdk.

    $ ./start


    once start script finishes

        It creates a container out of sample application.
        It sets up conjur OSS environment as containers.
        Logs into Conjur OSS environment  and adds secrets to secrets vault.
        Finally run sample client app, client app connects to Conjur OSS via Spring-boot-sdk and fetches required secrets from Conjur OSS.
        
4) Clean Up.

    $ ./stop

    Running stop removes the running Docker Compose containers of Sample app and Conjur OSS environment.

## Releases

TODO:
[Instructions for creating a new release]

## Contributing workflow

1. [Fork the project](https://help.github.com/en/github/getting-started-with-github/fork-a-repo)
2. [Clone your fork](https://help.github.com/en/github/creating-cloning-and-archiving-repositories/cloning-a-repository)
3. Make local changes to your fork by editing files
3. [Commit your changes](https://help.github.com/en/github/managing-files-in-a-repository/adding-a-file-to-a-repository-using-the-command-line)
4. [Push your local changes to the remote server](https://help.github.com/en/github/using-git/pushing-commits-to-a-remote-repository)
5. [Create new Pull Request](https://help.github.com/en/github/collaborating-with-issues-and-pull-requests/creating-a-pull-request-from-a-fork)

From here your pull request will be reviewed and once you've responded to all
feedback it will be merged into the project. Congratulations, you're a contributor!
