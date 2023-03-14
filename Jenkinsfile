pipeline {

    agent any

    stages {

        stage("Verify tooling") {
            steps {
                sh '''
                  docker version
                  docker info
                  docker compose version
                  curl --version
                  jq --version
                  docker compose ps
                '''
            }
        }

        stage('Fix maven executable') {
            steps {
                sh 'chmod +x mvnw'
            }
        }

        stage('Maven build') {
            environment {
                TINKOFF_API_TOKEN = credentials('TINKOFF_API_TOKEN')
            }
            tools {
                jdk "JDK-17"
            }
            steps {
                sh '''
                 export JAVA_HOME=/home/jenkins/tools/hudson.model.JDK/JDK-17/jdk-17.0.2
                 ./mvnw clean package -DTINKOFF_API_TOKEN="$TINKOFF_API_TOKEN"
                '''
            }
        }

        stage('Publish tests') {
            steps {
                publishCoverage adapters: [jacocoAdapter('target/site/jacoco/jacoco.xml')], sourceFileResolver: sourceFiles('STORE_LAST_BUILD')
            }
        }

        stage('Build docker image') {
            steps {
                sh 'docker build -t tportfolio .'
            }
        }

        stage("Build and start docker compose service") {
            environment {
                TINKOFF_API_TOKEN = credentials('TINKOFF_API_TOKEN')
            }
            steps {
                sh '''
                docker compose stop
                docker stop tportfolio_api || true && docker rm tportfolio_api || true
                docker stop tportfolio_mysql_db || true && docker rm tportfolio_mysql_db || true
                docker compose build --build-arg TINKOFF_API_TOKEN='$TINKOFF_API_TOKEN'
                docker compose up -d 
                '''
            }
        }
    }

    post {
        always {
            junit skipPublishingChecks: true, testResults: '**/target/surefire-reports/*.xml'
        }
    }
}
