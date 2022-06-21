//
//  Author: Hari Sekhon
//  Date: 2022-01-06 17:35:16 +0000 (Thu, 06 Jan 2022)
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

def call(timeoutMinutes=10){
  String label = "Terragrunt Plan - Dir: ${env.TERRAFORM_DIR ?: '.'}"
  // must differentiate lock to share the same lock as Terraform Plan and Terraform Apply
  String lock  = "Terraform - Dir: ${env.TERRAFORM_DIR ?: '.'}"
  lock(resource: lock, inversePrecedence: true) {
    // forbids older plans from starting
    milestone(ordinal: null, label: "Milestone: $label")

    // terragrunt docker image is pretty useless, doesn't have the tools to authenticate to cloud providers
    //container('terragrunt') {
      timeout(time: timeoutMinutes, unit: 'MINUTES') {
        //dir ("components/${COMPONENT}") {
        ansiColor('xterm') {
          dir(env.TERRAFORM_DIR ?: ".") {
            // alpine/terragrunt docker image doesn't have bash
            //sh '''#/usr/bin/env bash -euxo pipefail
            //sh '''#/bin/sh -eux
            echo 'Terragrunt Workspace List'
            sh (
              label: 'Workspace List',
              script: 'terragrunt workspace list || :'  // # 'workspaces not supported' if using Terraform Cloud as a backend
            )
            echo "$label"
            sh (
              label: "$label",
              script: 'terragrunt plan --terragrunt-non-interactive -out=plan.zip -input=false'  // # -var-file=base.tfvars -var-file="$ENV.tfvars"
            )
          }
        }
      }
    //}
  }
}
