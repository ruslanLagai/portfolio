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

        stage('Get code') {
            steps {
                // Get some code from a GitHub repository
                git branch: 'jenkins', url: 'https://github.com/ruslanLagai/portfolio'
                sh 'chmod +x mvnw'
            }
        }

        stage('Gradle build') {
            environment {
                TINKOFF_API_TOKEN = credentials('TINKOFF_API_TOKEN')
            }
            steps {
                sh './mvnw clean package -DTINKOFF_API_TOKEN='$TINKOFF_API_TOKEN''
            }
        }

        stage('Publish tests') {
            steps {
                publishCoverage adapters: [jacocoAdapter('target/site/jacoco-aggregate/jacoco.xml')], sourceFileResolver: sourceFiles('NEVER_STORE')
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
            junit '**/target/**/test-results/test/*.xml'
        }
    }
}
