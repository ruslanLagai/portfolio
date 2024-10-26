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

        stage('Build docker image') {
            environment {
                TINKOFF_API_TOKEN = credentials('TINKOFF_API_TOKEN')
            }
            steps {
                sh '''
                   docker build -t tportfolio .
                   docker build --target jacoco --output type=local,dest=jacoco .
                   docker build --target surefire --output type=local,dest=surefire .
                '''
            }
        }

        stage('Publish tests') {
            steps {
                recordCoverage(tools: [[parser: 'JACOCO']],
                        id: 'jacoco', name: 'JaCoCo Coverage',
                        sourceCodeRetention: 'EVERY_BUILD',
                        qualityGates: [
                                [threshold: 60.0, metric: 'LINE', baseline: 'PROJECT', unstable: true],
                                [threshold: 60.0, metric: 'BRANCH', baseline: 'PROJECT', unstable: true]])
            }
        }

        stage ('Dependency-Check') {
            steps {
                dependencyCheck additionalArguments: '''
                    -o "./"
                    -s "./"
                    -f "ALL"
                    --prettyPrint''', odcInstallation: 'Dependency Checker'

                dependencyCheckPublisher pattern: 'dependency-check-report.xml'
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
            junit skipPublishingChecks: true, testResults: '**/surefire/*.xml'
        }
    }
}
