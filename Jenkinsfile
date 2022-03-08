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

    sh """
      set -exuo pipefail
      git checkout "${sourceVersion}"
      echo "${targetVersion}" > VERSION
      ./build-package.sh
      summon -e artifactory ./publish.sh
      cp target/*.jar "${assetDirectory}"
    """
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
        sh '''
          version="$(<VERSION)"
          echo "${version}-SNAPSHOT" >> VERSION
        '''
      }
    }

    stage('Build') {
      steps {

        // create mvn settings.xml with repo creds pulled
        // from conjurops
        sh 'summon -e artifactory ./generate-maven-settings.sh'

        // Build Docker Image for tools (eg mvn)
        sh './build-tools-image.sh'

        // Run Docker Image to compile code and build jar
        sh './build-package.sh'

        publishHTML (target : [allowMissing: false,
          alwaysLinkToLastBuild: false,
          keepAll: true,
          reportDir: 'target/apidocs',
          reportFiles: 'index.html',
          reportName: 'Java Doc',
          reportTitles: 'Conjur Spring Boot SDK'])
      }
    }
  // Unit tests are now running from start.sh
  //   stage('UnitTest') {
  //     steps {
  //       sh './run-tests.sh'
  //     }
  //   }

    stage('functionalTests') {
      steps {
        dir ('SpringBootExample') {
          sh './build-sampleapp-image.sh'
          sh './start'
        }
        sh './run-tests.sh'
      }

      post {
        always {
          dir ('SpringBootExample') {
            sh './stop'
          }
        }
      }
    }
    stage('Report Test Coverage to Code Climate'){
      steps {
        dir('src/main/java'){
          ccCoverage('jacoco')
        }
        publishHTML (target : [allowMissing: false,
          alwaysLinkToLastBuild: false,
          keepAll: true,
          reportDir: 'target/site/jacoco/',
          reportFiles: 'index.html',
          reportName: 'Coverage Report',
          reportTitles: 'Conjur Spring Boot SDK Code Coverage Jacoco report'])
      }
    }

    stage('SpringBootExample') {
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
