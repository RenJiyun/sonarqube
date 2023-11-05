#!/bin/sh

./stop.sh

./gradlew build -x test -x :plugins:sonar-pmd-plugin:licenseMain --stacktrace "$@"
