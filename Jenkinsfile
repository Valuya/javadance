#!groovy

pipeline {
    agent any
    parameters {
        booleanParam(name: 'SKIP_TESTS', defaultValue: true, description: 'Skip tests')
        booleanParam(name: 'FORCE_DEPLOY', defaultValue: false, description: 'Force deploy on feature branches')
        string(name: 'ALT_DEPLOYMENT_REPOSITORY', defaultValue: '', description: 'Alternative deployment repo')
    }
    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    stages {
        stage ('Build') {
            steps {
                lock('gestemps') {
                    withMaven(maven: 'maven', mavenSettingsConfig: 'nexus-mvn-settings') {
                        sh "mvn -DskipTests=${params.SKIP_TESTS} clean compile install"
                    }
                }
            }
        }
        stage ('Publish') {
            when { anyOf {
                      environment name: 'BRANCH_NAME', value: 'master'
                      environment name: 'BRANCH_NAME', value: 'rc'
                      expression { return params.FORCE_DEPLOY == true }
            } }
            steps {
                script {
                    env.MVN_ARGS=""
                    if (params.ALT_DEPLOYMENT_REPOSITORY != '') {
                        env.MVN_ARGS="-DaltDeploymentRepository=${params.ALT_DEPLOYMENT_REPOSITORY}"
                    }
                }
                withMaven(maven: 'maven', mavenSettingsConfig: 'nexus-mvn-settings',
                          mavenOpts: '-DskipTests=true') {
                    sh "mvn deploy $MVN_ARGS"
                }
            }
        }
    }
}
