pipeline {

    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '30', daysToKeepStr: '15'))
    }

    environment {
        project_version = ""
        maas_version = ""
        release_url = ""
        // install_name ="${maas_id}-${maas_instance_id}"
        //IMAGE_ID="bla"
        //HASH="la"
    }

    // scm {
    //     git {
    //         remote {
    //             github('johnvincentcorpuz/jenkins-component-demo2')
    //             refspec('+refs/heads/*:refs/remotes/origin/* +refs/pull/*:refs/remotes/origin/pr/*')
    //         }
    //         branch('**')
    //     }
    // }

    // triggers {
    //     githubPullRequest {
    //         triggerPhrase('run test')
    //         onlyTriggerPhrase()
    //         useGitHubHooks()
    //         permitAll()
    //         autoCloseFailedPullRequests()
    //         displayBuildErrorsOnDownstreamBuilds()
    //         allowMembersOfWhitelistedOrgsAsAdmin()
    //         extensions {
    //             commitStatus {
    //                 context('deploy to staging site')
    //                 triggeredStatus('starting deployment to staging site...')
    //                 startedStatus('deploying to staging site...')
    //                 addTestResults(true)
    //                 statusUrl('http://mystatussite.com/prs')
    //                 completedStatus('SUCCESS', 'All is well')
    //                 completedStatus('FAILURE', 'Something went wrong. Investigate!')
    //                 completedStatus('PENDING', 'still in progress...')
    //                 completedStatus('ERROR', 'Something went really wrong. Investigate!')
    //             }
    //             buildStatus {
    //                 completedStatus('SUCCESS', 'There were no errors, go have a cup of coffee...')
    //                 completedStatus('FAILURE', 'There were errors, for info, please see...')
    //                 completedStatus('ERROR', 'There was an error in the infrastructure, please contact...')
    //             }
    //         }
    //     }
    // }

    stages {
        stage('Setup') {
            steps {
                checkout scm
                script {
                    sh('ls')
                }
            }
        }
        stage('Install') {
            steps {
              
                script {
                    sh ('ls')
                }
            }
        }
        stage('Test') {
            steps {
                sh ('ls')
            }
        }
    }

    post {
        always {
            timestamps {
                echo "Finished Running Job"
                echo "Environment Variables"
                sh('printenv')
            }
        }
        success {
            timestamps {
                script {
                    echo "success"
                    // githubNotify description: 'This is a shorted example',  status: 'SUCCESS'
                    // githubNotify account: 'johnvincentcorpuz', context: 'Final Test', credentialsId: johngithub,
                    //     description: 'This is an example', repo: 'jenkins-component-demo2', sha: env.GITHUB_PR_HEAD_SHA, status: 'SUCCESS', targetUrl: 'https://my-jenkins-instance.com'
                    def payload = """
                         {
                              "state": "success",
                              "target_url": "https://example.com/build/status",
                              "description": "The build succeeded!",
                              "context": "continuous-integration/jenkins"
                         }
                    """
                    httpRequest acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: payload, url: "${env.GITHUB_PR_HEAD_SHA}"
               
                }
            }
        }
        failure {
            timestamps {
                script {
                    echo "sending failure message"
                }
            }
        }
        aborted {
            timestamps {
                script {
                    echo "aborted failure message"
                }
            }
        }
    }
}



