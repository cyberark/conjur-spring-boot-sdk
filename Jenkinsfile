#!/usr/bin/env groovy

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
  release.promote(params.VERSION_TO_PROMOTE) { sourceVersion, targetVersion, assetDirectory ->
    // Any assets from sourceVersion Github release are available in assetDirectory
    // Any version number updates from sourceVersion to targetVersion occur here
    // Any publishing of targetVersion artifacts occur here
    // Anything added to assetDirectory will be attached to the Github Release
    env.ASSET_DIR = assetDirectory

    // This call downloads the pre-release jar from artifactory, modifies its version
    // and publishes it to the releases repo in artifactory.
    // It also copies the release jar to ASSET_DIR for inclusion in the Github
    // Release
    release.publishJava('spring-boot-conjur', sourceVersion, targetVersion)
  }
  return
}

pipeline {
  agent { label 'executor-v2' }

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
    // Generates a VERSION file based on the current build number and latest version in CHANGELOG.md
    stage('Validate Changelog and set version') {
      steps {
        updateVersion("CHANGELOG.md", "${BUILD_NUMBER}")
      }
    }

    stage('Build') {
      steps {
        // Build Docker Image for tools (eg mvn)
        sh './build-tools-image.sh'

        // Run Docker Image to compile code and build jar
        sh './build-package.sh'

        // Build Docker image and push to internal artifactory
        sh './build-app-image.sh'
      }
    }

    stage('UnitTest') {
      steps {
        sh './run-tests.sh'
      }
    }

    stage('functionalTests') {
      steps {
        dir ('functionaltests') {
          sh './start'
        }
      }

      post {
        always {
          dir ('functionaltests') {
            sh './stop'
          }
        }
      }
    }
    stage('Report Test Coverage to Code Climate'){
      steps {
        archiveArtifacts(artifacts:'jacoco.html')
        dir('src/main/java'){
          ccCoverage('jacoco')
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
        release { billOfMaterialsDirectory, assetDirectory ->
          // Publish release artifacts to all the appropriate locations
          // Copy any artifacts to assetDirectory to attach them to the Github release
          sh "ASSET_DIR=\"${assetDirectory}\" summon -e artifactory ./publish.sh"
        }
      }
    }
  }

  post {
    always {
      cleanupAndNotify(currentBuild.currentResult)
    }
  }
}
