#!/bin/bash

# deloy to the debug environment

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

DIST_DIR=$DIR/sonar-application/build/distributions
DEPLOY_DIR=/home/ren/debug

cp $DIST_DIR/sonar-application-9.9.3-SNAPSHOT.zip $DEPLOY_DIR

cd $DEPLOY_DIR
unzip sonar-application-9.9.3-SNAPSHOT.zip

./sonarqube-9.9.3-SNAPSHOT/bin/linux-x86-64/sonar.sh console