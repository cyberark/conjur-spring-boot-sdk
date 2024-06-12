#!/usr/bin/env groovy
@Library("product-pipelines-shared-library") _

// This is a template Jenkinsfile for builds and the automated release project

// Automated release, promotion and dependencies
properties([
  // Include the automated release parameters for the build
  release.addParams(),
  // Dependencies of the project that should trigger builds
  dependencies([])
])

// Performs release promotion.  No other stages will be run
if (params.MODE == "PROMOTE") {
  release.promote(params.VERSION_TO_PROMOTE) { infrapool, sourceVersion, targetVersion, assetDirectory ->
    // Any assets from sourceVersion Github release are available in assetDirectory
    // Any version number updates from sourceVersion to targetVersion occur here
    // Any publishing of targetVersion artifacts occur here
    // Anything added to assetDirectory will be attached to the Github Release

    // Pass assetDirectory through to publish.sh as an env var.
    env.ASSET_DIR=assetDirectory

    infrapool.agentSh """
      git checkout "v${sourceVersion}"
      echo -n "${targetVersion}" > VERSION
      cp VERSION VERSION.original
      ./build-tools-image.sh
      ./build-package.sh
      summon ./publish.sh
      cp target/*.jar "${assetDirectory}"
    """
  }
  return
}

pipeline {
  agent { label 'conjur-enterprise-common-agent' }

  options {
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '30'))
  }

  triggers {
    cron(getDailyCronString())
  }

  environment {
    // Sets the MODE to the specified or autocalculated value as appropriate
    MODE = release.canonicalizeMode()
  }

  stages {
    // Aborts any builds triggered by another project that wouldn't include any changes
    stage ("Skip build if triggering job didn't create a release") {
      when {
        expression {
          MODE == "SKIP"
        }
      }
      steps {
        script {
          currentBuild.result = 'ABORTED'
          error("Aborting build because this build was triggered from upstream, but no release was built")
        }
      }
    }

    stage('Get InfraPool ExecutorV2 Agent') {
      steps {
        script {
          // Request ExecutorV2 agents for 1 hour(s)
          INFRAPOOL_EXECUTORV2_AGENT_0 = getInfraPoolAgent.connected(type: "ExecutorV2", quantity: 1, duration: 1)[0]
          // Get submodules - can be removed when submodule is migrated to Github Enterprise
          INFRAPOOL_EXECUTORV2_AGENT_0.agentSh 'git submodule update --init --recursive'
        }
      }
    }

    // Generates a VERSION file based on the current build number and latest version in CHANGELOG.md
    stage('Validate Changelog and set version') {
      steps {
        script {
          updateVersion(INFRAPOOL_EXECUTORV2_AGENT_0, "CHANGELOG.md", "${BUILD_NUMBER}")
          INFRAPOOL_EXECUTORV2_AGENT_0.agentSh '''
            cp VERSION VERSION.original
            version="$(<VERSION)"
            echo "${version}-SNAPSHOT" >> VERSION
            cp VERSION VERSION.snapshot
          '''
        }
      }
    }

    stage('Build') {
      steps {
        script {
          // Build Docker Image for tools (eg mvn)
          INFRAPOOL_EXECUTORV2_AGENT_0.agentSh './build-tools-image.sh'

          // Run Docker Image to compile code and build jar
          INFRAPOOL_EXECUTORV2_AGENT_0.agentSh './build-package.sh'
          INFRAPOOL_EXECUTORV2_AGENT_0.agentStash name: 'api-docs', includes: 'target/apidocs/*'
          unstash 'api-docs'

          publishHTML (target : [allowMissing: false,
            alwaysLinkToLastBuild: false,
            keepAll: true,
            reportDir: 'target/apidocs',
            reportFiles: 'index.html',
            reportName: 'Java Doc',
            reportTitles: 'Conjur Spring Boot SDK'])
        }
      }
    }
  // Unit tests are now running from start.sh
  //   stage('UnitTest') {
  //     steps {
  //      script {
  //       INFRAPOOL_EXECUTORV2_AGENT_0.agentSh './run-tests.sh'
  //      }
  //     }
  //   }

    stage('Functional Tests OSS') {
      steps {
        script {
          dir ('SpringBootExample') {
            INFRAPOOL_EXECUTORV2_AGENT_0.agentSh './build-sampleapp-image.sh'
            INFRAPOOL_EXECUTORV2_AGENT_0.agentSh './start'
          }
        }
      }
      post {
        always {
          script {
            INFRAPOOL_EXECUTORV2_AGENT_0.agentArchiveArtifacts artifacts: 'SpringBootExample/logs/*'
            INFRAPOOL_EXECUTORV2_AGENT_0.agentStash name: 'surefire-reports-oss', includes: 'target/surefire-reports/*.xml'
            unstash 'surefire-reports-oss'
            junit 'target/surefire-reports/*.xml'
            dir ('SpringBootExample') {
              // Jenkins has already recorded these results
              // clean them out so they aren't recorded for
              // a second time after the Enterprise tests.
              INFRAPOOL_EXECUTORV2_AGENT_0.agentSh './clean_surefire_reports'
              INFRAPOOL_EXECUTORV2_AGENT_0.agentSh './stop'
            }
          }
        }
      }
    }
    stage('Functional Tests Enterprise') {
      steps {
        script {
          dir ('SpringBootExample') {
            INFRAPOOL_EXECUTORV2_AGENT_0.agentSh './start_enterprise'
          }
        }
      }
      post {
        always {
          script {
            INFRAPOOL_EXECUTORV2_AGENT_0.agentStash name: 'surefire-reports-enterprise', includes: 'target/surefire-reports/*.xml'
            unstash 'surefire-reports-enterprise'
            junit 'target/surefire-reports/*.xml'
            dir ('SpringBootExample') {
              INFRAPOOL_EXECUTORV2_AGENT_0.agentSh './stop_enterprise'
            }
          }
        }
      }
    }
    stage('Report Test Coverage to Codacy'){
      steps {
        script {
          dir('src/main/java'){
            INFRAPOOL_EXECUTORV2_AGENT_0.agentStash name: 'jacoco', includes: 'jacaco.xml'
            unstash 'jacoco'
            codacy action: 'reportCoverage', filePath: "jacoco.xml"
          }
          INFRAPOOL_EXECUTORV2_AGENT_0.agentStash name: 'target-site-jacoco', includes: 'target/site/jacoco/*.xml'
          unstash 'target-site-jacoco'
          publishHTML (target : [allowMissing: false,
            alwaysLinkToLastBuild: false,
            keepAll: true,
            reportDir: 'target/site/jacoco/',
            reportFiles: 'index.html',
            reportName: 'Coverage Report',
            reportTitles: 'Conjur Spring Boot SDK Code Coverage Jacoco report'])
        }
      }
    }

    stage('Release') {
      when {
        expression {
          MODE == "RELEASE"
        }
      }

      steps {
        script {
          // VERSION-SNAPSHOT is not valid for the release process.
          INFRAPOOL_EXECUTORV2_AGENT_0.agentSh 'cp VERSION.original VERSION'
          release(INFRAPOOL_EXECUTORV2_AGENT_0) { billOfMaterialsDirectory, assetDirectory ->
            // Publish release artifacts to all the appropriate locations
            // Copy any artifacts to assetDirectory to attach them to the Github release
            INFRAPOOL_EXECUTORV2_AGENT_0.agentSh "ASSET_DIR=\"${assetDirectory}\" summon ./publish.sh"
          }
        }
      }
    }
  }

  post {
    always {
      script {
        releaseInfraPoolAgent(".infrapool/release_agents")
      }
    }
  }
}
