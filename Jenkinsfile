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
  release.promote(params.VERSION_TO_PROMOTE) { sourceVersion, targetVersion, assetDirectory ->
    // Any assets from sourceVersion Github release are available in assetDirectory
    // Any version number updates from sourceVersion to targetVersion occur here
    // Any publishing of targetVersion artifacts occur here
    // Anything added to assetDirectory will be attached to the Github Release

    // Pass assetDirectory through to publish.sh as an env var.
    env.ASSET_DIR=assetDirectory

    sh """
      export ASSET_DIR="${env.ASSET_DIR}"
      export MODE="${params.MODE}"
      git checkout "v${sourceVersion}"
      echo -n "${targetVersion}" > VERSION
      cp VERSION VERSION.original
      ./build-tools-image.sh
      ./build-package.sh
      summon ./publish.sh
      cp target/*.jar "${assetDirectory}"
    """
  }

  release.copyEnterpriseRelease(params.VERSION_TO_PROMOTE)
  return
}

pipeline {
  agent { label 'conjur-enterprise-AmznDocker' }

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
        script {
          updateVersion("CHANGELOG.md", "${BUILD_NUMBER}")
          sh '''
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
          // Initialize submodules
          sh 'git submodule update --init --recursive'

          // Build Docker Image for tools (eg mvn)
          sh './build-tools-image.sh'

          // Run Docker Image to compile code and build jar
          sh './build-package.sh'
        }
      }
    }
  // Unit tests are now running from start.sh
  //   stage('UnitTest') {
  //     steps {
  //      script {
  //       sh './run-tests.sh'
  //      }
  //     }
  //   }

    stage('Functional Tests OSS') {
      steps {
        script {
          dir ('SpringBootExample') {
            sh './build-sampleapp-image.sh'
            sh './start'
          }
        }
      }
      post {
        always {
          script {
            normalizeOwnership('target', 'SpringBootExample/logs')
            archiveArtifacts artifacts: 'SpringBootExample/logs/*'
            junit 'target/surefire-reports/*.xml'
            dir ('SpringBootExample') {
              // Jenkins has already recorded these results
              // clean them out so they aren't recorded for
              // a second time after the Enterprise tests.
              sh './clean_surefire_reports'
              sh './stop'
            }
          }
        }
      }
    }

    stage('Functional Tests Enterprise') {
      steps {
        script {
          dir ('SpringBootExample') {
            sh './start_enterprise'
          }
        }
      }
      post {
        always {
          script {
            normalizeOwnership('target')
            junit 'target/surefire-reports/*.xml'
            dir ('SpringBootExample') {
              sh './stop_enterprise'
            }
          }
        }
      }
    }

    stage('Functional Tests Cert Auth (Enterprise)') {
      steps {
        script {
          dir ('SpringBootExample') {
            sh './start-cert'
          }
        }
      }
      post {
        always {
          script {
            normalizeOwnership('target', 'SpringBootExample/logs')
            archiveArtifacts artifacts: 'SpringBootExample/logs/*', allowEmptyArchive: true
            dir ('SpringBootExample') {
              sh './stop-cert'
            }
          }
        }
      }
    }

    stage('Report Test Coverage to Codacy'){
      steps {
        script {
          dir('src/main/java'){
            codacy action: 'reportCoverage', filePath: "jacoco.xml"
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
          sh 'cp VERSION.original VERSION'
          release { billOfMaterialsDirectory, assetDirectory ->
            // Publish release artifacts to all the appropriate locations
            // Copy any artifacts to assetDirectory to attach them to the Github release
            sh "ASSET_DIR=\"${assetDirectory}\" summon ./publish.sh"
          }
        }
      }
    }
  }
}

// Fixes root ownership left behind by Docker volume mounts so that subsequent
// Jenkins steps and re-runs can read and write the affected directories.
void normalizeOwnership(String... dirs) {
  sh """
    if ! command -v sudo >/dev/null 2>&1; then
      exit 0
    fi
    u="\$(id -u):\$(id -g)"
    for d in ${dirs.join(' ')}; do
      if [ -e "\$d" ]; then
        sudo chown -R "\$u" "\$d" 2>/dev/null || true
        sudo chmod -R u+rwX "\$d" 2>/dev/null || true
      fi
    done
  """
}
