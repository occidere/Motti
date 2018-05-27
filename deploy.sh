# !/bin/bash

project_name=${PWD##*/}
local_occidere_maven_repo='/mnt/c/Users/occid/IdeaProjects/occidere-maven-repo'

mvn -Dmaven.test.skip=true -DaltDeploymentRepository=snapshot-repo::default::file://${local_occidere_maven_repo}/snapshots clean deploy

cd ${local_occidere_maven_repo}

# ssh -T git@github.com

git status
git add .
git status
git commit -m "release new version of ${project_name}"
git push origin master

