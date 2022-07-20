//
//  Author: Hari Sekhon
//  Date: 2021-04-30 15:25:01 +0100 (Fri, 30 Apr 2021)
//
//  vim:ts=2:sts=2:sw=2:et
//
//  https://github.com/HariSekhon/Jenkins
//
//  License: see accompanying Hari Sekhon LICENSE file
//
//  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help steer this or other code I publish
//
//  https://www.linkedin.com/in/HariSekhon
//

// ========================================================================== //
//             Find Git Log Committers to Notify they Broke the Build
// ========================================================================== //

// abstracted from previous pipelines gitMergePipeline, terraformPipe, jenkinsBackupJobConfigsPipeline

// returns Map of committers since last successful build, in format ['username': 'email']

// Example Usage:
//
//      failure {
//        script {
//          Map committers = gitLogBrokenCommitters()
//          // send notification to these users
//          // see slackBrokenCommitters.groovy and slackNotify.groovy for examples
//        }
//      }

// matcher is non-serializable and will result in this exception in Jenkins due to it wanting to be able to save state to disk for durability/resume:
//
//  Caused: java.io.NotSerializableException: java.util.regex.Matcher
//
// this annotation tells it not to try to save the local variables of this function
@NonCPS
def call() {
  // gets a List in ['username<email>'] format
  List logCommittersList = sh (
    label: 'Get Git Log Committers Since Last Successful Build',
    returnStdout: true,
    script: '''
      set -eux
      if [ -z "${GIT_PREVIOUS_SUCCESSFUL_COMMIT:-}" ]; then
        exit 0
      fi
      git log --format='%an <%ae>' "${GIT_PREVIOUS_SUCCESSFUL_COMMIT}..${GIT_COMMIT}" |
      grep -Fv -e Jenkins \
               -e '[bot]' |
      sort -fu
    '''
  ).trim().split('\n').collect{ it.trim() }
  echo "Inferred Git committers since last successful build via git log to be: $logCommittersList"
  // gets a Map in ['user': 'email'] format
  Map logCommitters = [:]
  logCommittersList.each {
    if((match = it =~ /^(.+)<(.+)>$/)){
      username = match.group(1)
      email = match.group(2)
      logCommitters.put(username, email)
    } else {
      warning("failed to parse username<email>: $it")
    }
  }
  echo "Inferred Git committers since last successful build via git log to be: $logCommitters"
  return logCommitters
}
