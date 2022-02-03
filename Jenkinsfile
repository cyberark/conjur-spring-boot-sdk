pipeline {
  agent { label 'executor-v2' }
  stages {
    stage('Whats in my workspace?') {
      steps {
	      sh 'ls'
      }
    }
    stage('install Java') {
      steps {
	      sh 'docker build --build-arg java_version=11 .'
      }
    }
  }
}
