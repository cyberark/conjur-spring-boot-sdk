# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.1.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Added support for **client certificate authentication (`authn-cert` / mTLS)**
  - New configuration properties:
    - `conjur.authn-cert-service-id` / `CONJUR_AUTHN_CERT_SERVICE_ID` — service ID of the `authn-cert` authenticator (e.g. `acme-vm`)
    - `conjur.client-cert-file` / `CONJUR_CLIENT_CERT_FILE` — path to the PEM-encoded X.509 client certificate
    - `conjur.client-key-file` / `CONJUR_CLIENT_KEY_FILE` — path to the PEM-encoded PKCS#8 private key

## [2.3.0] - 2025-12-12

### Updated
- Major updates: Spring Boot 3.5.7, Java 17, Jakarta migration, Resilience4j 2.3.0
- Other updates: maven-compiler-plugin 3.13.0, maven-surefire-plugin 3.2.5, maven-source-plugin 3.3.1, maven-javadoc-plugin 3.11.2, maven-gpg-plugin 3.2.7, okhttp3 4.12.0, junit-bom 5.11.4, jacoco-maven-plugin 0.8.12
- Notes: Migrated from javax to jakarta namespaces for Spring Boot 3 compatibility

## [2.2.5] - 2025-11-18

### Added
- Added additional step for Copying Github Enterprise release to Github in the PROMOTE step of the Jenkins pipeline

## [2.2.4] - 2025-11-18

### Changed
- Change codeowners to conjur-integrations

## [2.2.3] - 2025-10-16

### Added
- Added `close-stale.yml` GitHub workflow

## [2.2.2] - 2025-08-06

### Changed
- Updated 'conjur-sdk-java' dependency version to 4.2.2

## [2.2.1] - 2025-08-05

### Changed
- Retrieve SDK version from CHANGELOG instead of VERSION file (CNJR-10604)

## [2.2.0] - 2025-07-16
- Added support to fail the microservice/application bootup in case of non-availability of secrets
- Disable logging off authentication tokens or api keys

## [2.1.3] - 2025-04-01
- Added telemetry headers

## [2.1.2] - 2024-07-26
- Fix for downgrading the logging level from error to warn if no data found for variable
- Fix for retrieving secrets with long key path in processMultipleSecretResult() for @ConjurValues (Bulk Retrieval)

## [2.1.1] - 2024-06-12
- Fix for @ConjurValues annotation always returning null value.
- Fix to scan only for Conjur related variables
- Mapping for external property files
- Externalising the mapping properties file path as env or in property file

## [2.1.0] - 2023-07-28
### Security
- Upgraded Spring Boot Starter Parent to 2.7.12 and Junit to 5.9.3
  [conjur-spring-boot-sdk#97](https://github.com/cyberark/conjur-spring-boot-sdk/pull/97)

## [2.0.2] - 2023-07-24
- Updated to support JDK version 1.8

## [2.0.1] - 2023-07-19
### Added
- Updated conjur-java-sdk to the version 4.1.0

## [2.0.0]
- Plugin now supports Spring Cloud Configuration and to dynamically inject secrets to application.

## [1.1.0]
- Update Spring Boot Starter Parent version to 3.0.5 to address Security issues reported through Snyk.
- Fixed issue: https://github.com/cyberark/conjur-spring-boot-sdk/issues/84
- Fixed issue: https://github.com/cyberark/conjur-spring-boot-sdk/issues/82
- Fixed issue: https://github.com/cyberark/conjur-spring-boot-sdk/issues/80
- https://github.com/cyberark/conjur-spring-boot-sdk/issues/78
- https://github.com/cyberark/conjur-spring-boot-sdk/issues/75
- https://github.com/cyberark/conjur-spring-boot-sdk/issues/64
- https://github.com/cyberark/conjur-spring-boot-sdk/issues/63

## [1.0.3] - 2023-04-20
### Added

## [1.0.2] - 2022-04-06

## [1.0.1] - 2022-04-05

### Added
- Conjur Enterprise Tests
- Publication to Maven Central
- Code Signing

## [0.0.1] - 2022-02-03

### Added
- Initial Code Import
- Sample Application
- Test Cases & Infrastructure

### Changed

### Deprecated

### Removed

### Fixed

### Security
- Updated Spring Boot to 2.5.12 [cyberark/conjur-spring-boot-sdk#59](https://github.com/cyberark/conjur-spring-boot-sdk/pull/59)
