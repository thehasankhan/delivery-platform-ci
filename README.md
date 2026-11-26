# Delivery Platform CI

This Jenkins Shared Library builds, tests, and smoke-tests a Dockerized application.
The consuming repository provides build and smoke commands.

## Configure Jenkins

Register an untrusted shared library named delivery-platform-ci using Modern SCM
and Git. Pin its default version to a reviewed tag or commit. Point each consumer's
Pipeline job at its repository, branch, and Jenkinsfile. Install Pipeline, Git,
and JUnit, and provide a Linux agent labeled docker-linux with Docker and the
application's build tools.

## Onboard an application

Add a Dockerfile and a Jenkinsfile based on the following example.

    @Library('delivery-platform-ci') _

    deliveryPipeline(
        buildCommand: './mvnw -B -ntp clean verify',
        testReports: 'target/surefire-reports/TEST-*.xml',
        imageRepository: 'example-api',
        containerPort: 8080,
        smokeCommand: 'sh ci/smoke.sh'
    )

An npm project can supply its own npm build/test command and JUnit XML report
path. Use a distinct image repository name for each job on a shared Docker
engine. The optional settings are agentLabel (docker-linux), platform
(linux/amd64), dockerfile (Dockerfile), and buildContext (.).

The smoke script receives SMOKE_BASE_URL with the assigned localhost port.
Wait for readiness with a bounded retry, check an application response, and
exit nonzero on failure. Use a test-safe configuration and synthetic data.
The library removes the smoke-test container when the stage exits.

Run application-controlled commands on isolated agents. Keep production data,
cluster credentials, and registry publishing credentials out of validation
jobs. This version builds images into the agent's local Docker engine.
