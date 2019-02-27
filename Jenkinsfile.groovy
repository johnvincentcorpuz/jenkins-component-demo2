import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovyx.net.http.RESTClient

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
                script {
                    sh ('ls')
                    sh ('exit 1')
                }

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
                    notify_commit_status("success")
                }
            }
        }
        failure {
            timestamps {
                script {
                    echo "sending failure message"
                    notify_commit_status("failure")
                }
            }
        }
        aborted {
            timestamps {
                script {
                    echo "aborted failure message"
                    notify_commit_status("error")
                }
            }
        }
    }
}



def getBranchHead(org,repository,branch) {

    def requestUrl = "https://api.github.com/repos/${org}/${repository}/git/refs/heads/${branch}"
    withCredentials([string(credentialsId: 'gitPrivateToken', variable: 'GITHUB_TOKEN')]) {
        def gitHubBranchHead = new RESTClient( 'https://api.github.com/repos/${org}/${repository}/git/refs/heads/${branch}' )
        gitHubBranchHead.headers.'Authorization' = "token ${GITHUB_TOKEN}"
        def resp = gitHubBranchHead.get()
        print resp
    }

    
    def response = httpRequest authentication: 'johngithub', httpMode: 'GET', url: requestUrl
    print response.status
    print response.content

    def parser = new JsonSlurper()
    def jsonResp = parser.parseText(response.content)

    return jsonResp?.object?.sha

}


def notify_commit_status(result){
    //GITHUB_REPO_SSH_URL=git@github.com:johnvincentcorpuz/jenkins-component-demo2.git
    //def gitInfo = env.GITHUB_REPO_SSH_URL.minus("git@github.com:").minus(".git").split("/")
    def gitInfo= scm.userRemoteConfigs[0].url
    gitInfo = gitInfo.minus("git@github.com:").minus(".git").split("/")
    print("git Info: ${gitInfo}")
    def gitOrg =  gitInfo[0]
    def gitRepo = gitInfo[1]
        
    print("Git ORG: ${gitOrg}")
    print("Git Repo: ${gitRepo}")

    //Todo: Enable in Jenkins Prod
    // def gitInfo = gitUrl.minus("git@github.com:").minus(".git").split("/")
    // def gitOrg =  gitInfo[0]
    // def gitRepo = gitInfo[1]

    //Todo: Change GIT_HUB_BRANCH_NAME to BRANCH_NAME, plugin dependents
    def branchHeadSha = getBranchHead(gitOrg,gitRepo,env.BRANCH_NAME)



    def payload = [
          state: "${result}",
          target_url: "${env.BUILD_URL}",
          description: "Unit Test Passed",
          context: "jenkins/unit-tests"
        ]

    payload = JsonOutput.toJson(payload)

    def url = "https://api.github.com/repos/${gitOrg}/${gitRepo}/statuses/${branchHeadSha}"
    //print "statusUrl: ${url}"
    httpRequest authentication: 'johngithub', acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: payload, url: url

}