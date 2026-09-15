# CLAUDE.md

## Overview

This repo is the **Conjur Spring Boot SDK** (`com.cyberark:conjur-sdk-springboot`), a CyberArk-maintained, "Certified"-tier library that lets Spring Boot applications retrieve secrets from **CyberArk Conjur** (OSS, Enterprise, or Cloud) as externalized configuration. Applications integrate it either declaratively via the `@ConjurPropertySource`/`@ConjurValue` annotations (minimal code change) or with no code change by mapping `application.properties` placeholders to Conjur variables. It wraps the underlying `conjur-sdk-java` client and exposes Conjur secrets to Spring's `Environment`/`@Value` machinery, with optional resilience4j-based retry for transient auth/connection failures.

## Tech stack

- Java 17, Maven (single module, packaging `jar`)
- Spring Boot 3.x (`spring-boot-starter-parent` 3.5.14), Spring Boot autoconfiguration (`spring.factories` + `AutoConfiguration.imports`)
- `com.cyberark:conjur-sdk-java` (4.2.2) for the actual Conjur API/auth calls
- resilience4j-spring-boot3 for retry handling
- JUnit 5 + spring-boot-starter-test for tests; JaCoCo for coverage
- Docker / docker-compose used only for the integration-test sample app, not for the library build itself

## Build

Standard Maven build from the repo root:
```
mvn install -DskipTests      # install SDK jar to local ~/.m2
mvn -f pom.xml clean package -Dmaven.test.skip   # build jar only (mirrors build-package.sh)
```
`build-package.sh` is the CI packaging script: it runs `mvn versions:set` using the version from `CHANGELOG.md`/`VERSION`, then packages, inside a `tools` Docker image (see `build-tools-image.sh` / `tools.Dockerfile`). Do not hand-edit the `<version>` in `pom.xml` — it's a placeholder overwritten by that script from `CHANGELOG.md`.

## Tests

- Unit tests (the bulk of test coverage, under `src/test/java`) run with plain Maven:
  ```
  mvn test
  mvn jacoco:prepare-agent test jacoco:report   # with coverage report
  ```
- `run-tests.sh` at the repo root is currently a stub — it just prints `"Tests are currently run from SpringBootExample/start"` and does nothing else (the real docker/CI logic is commented out).
- Full integration testing against a real Conjur OSS instance happens via the sample app:
  ```
  cd SpringBootExample
  ./build-sampleapp-image.sh
  ./start     # spins up Conjur OSS + sample app in docker-compose, seeds secrets, runs mvn test against a live Conjur
  ./stop      # tears everything down
  ```
  (`start_enterprise`/`stop_enterprise` do the same against Conjur Enterprise.) This requires Docker.

## Repo structure

- `src/main/java/com/cyberark/conjur/springboot/`
  - `annotations/` — public API: `@ConjurPropertySource(s)`, `@ConjurValue(s)`, `Registrar` (imported by the annotations to wire property sources)
  - `core/env/` — `ConjurConfig`, `ConjurConnectionManager`, `AccessTokenProvider`, `ConjurPropertySource` (the `PropertySource` implementation that talks to Conjur)
  - `processor/` — `SpringBootConjurAutoConfiguration` (Spring Boot autoconfig entry point), `ConjurCloudProcessor`, `ConjurValueClassProcessor`, `ConjurRetrieveSecretService`
  - `service/` — property-source/processor chain implementations (`PropertyProcessorChain`, `DefaultPropertySourceChain`, `CustomPropertySourceChain`)
  - `util/` — `ConjurResilientAuthenticator`, `ResilienceRegistryConfig`/`Holder`, `ConjurSecretUtils`
  - `domain/` — `ConjurProperties` (bindable `conjur.*` config properties)
  - `constant/ConjurConstant.java`
- `src/main/resources/META-INF/` — `spring.factories` and `spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` register the autoconfiguration; keep both in sync when adding/renaming autoconfig classes (Spring Boot 2.x vs 3.x mechanisms).
- `src/test/java/...` — unit tests mirroring the main package structure; `src/test/resources/` has sample `application.properties`/`conjur.properties`/`testParameters.properties` fixtures.
- `SpringBootExample/` — a standalone sample Spring Boot client app (own `pom.xml`) demonstrating real integration; also doubles as the integration-test harness (see Tests above). Contains its own Dockerfile, docker-compose config, and `conf/`/`tls/` for a local Conjur+proxy stack.
- `mvn-settings.xml` — Maven settings used for deploy/publish (`publish.sh`), not needed for a plain local build.
- `Jenkinsfile`, `.devops/` — CI pipeline definitions (Jenkins-based).
- `CHANGELOG.md` — source of truth for the release version consumed by `build-package.sh`.

## Conventions / gotchas

- This is a library, not a runnable app — there's no `main` in `src/main`; runnable behavior only exists in `SpringBootExample`.
- Conjur connection parameters (`conjur.account`, `conjur.appliance-url`, `conjur.authn-login`, `conjur.authn-api-key`, `conjur.cert-file`, etc.) are documented in `README.md`; retry behavior is opt-in via `conjur.resilience-enabled` (default `false`) plus `conjur.resilience-max-attempts`/`conjur.resilience-wait-duration`.
- Both Spring Boot 2.x (`spring.factories`) and 3.x (`AutoConfiguration.imports`) autoconfiguration registration files exist — update both if you add a new `@Configuration`/autoconfig class.
- `mvn` commands in CI scripts (`build-package.sh`, `publish.sh`, integration `start`) run inside a custom `tools`/`app` Docker image (built via `build-tools-image.sh`), not on the host directly — replicate that if debugging CI-only failures.
- Never commit real Conjur secrets/certs; `secrets.yml`, `.env`, and generated `api_key`/`*.pem` files under `SpringBootExample` are test-only artifacts created by `start`/`stop`.
