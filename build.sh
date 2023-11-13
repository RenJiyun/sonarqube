#!/bin/sh

./stop.sh

./gradlew build \
    -x test \
    -x :plugins:sonar-pmd-plugin:licenseMain \
    -x :plugins:sonar-cia-plugin:licenseMain \
    --stacktrace "$@"
