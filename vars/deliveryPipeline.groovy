def call(Map config) {
    for (String key : ['buildCommand', 'testReports', 'imageRepository', 'containerPort', 'smokeCommand']) {
        if (!config[key]?.toString()?.trim()) {
            error('Missing deliveryPipeline setting: ' + key)
        }
    }

    def buildAgent = config.agentLabel ?: 'docker-linux'
    def imageTag = config.imageRepository + ':ci-' + env.BUILD_NUMBER
    def platform = config.platform ?: 'linux/amd64'
    def smokeScript = libraryResource('delivery-platform/smoke.sh')

    pipeline {
        agent { label buildAgent }
        options {
            disableConcurrentBuilds()
            skipStagesAfterUnstable()
            timeout(time: 30, unit: 'MINUTES')
        }

        stages {
            stage('Build and test') {
                steps {
                    sh config.buildCommand
                }
                post {
                    always {
                        junit testResults: config.testReports, skipPublishingChecks: true
                    }
                }
            }

            stage('Build image') {
                steps {
                    withEnv([
                        'CI_IMAGE=' + imageTag,
                        'CI_PLATFORM=' + platform,
                        'CI_DOCKERFILE=' + (config.dockerfile ?: 'Dockerfile'),
                        'CI_BUILD_CONTEXT=' + (config.buildContext ?: '.')
                    ]) {
                        sh 'docker build --platform "$CI_PLATFORM" -f "$CI_DOCKERFILE" -t "$CI_IMAGE" "$CI_BUILD_CONTEXT"'
                    }
                }
            }

            stage('Smoke test') {
                steps {
                    withEnv([
                        'CI_IMAGE=' + imageTag,
                        'CI_PLATFORM=' + platform,
                        'CI_CONTAINER_PORT=' + config.containerPort,
                        'CI_SMOKE_COMMAND=' + config.smokeCommand
                    ]) {
                        sh smokeScript
                    }
                }
            }
        }
    }
}
