#!/bin/bash

# ./dev-build.sh jar for cross platfor jar build only
if [ "$1" = "jar" ]; then
        ./gradlew :jvm:workbookapp:build ;
# ./dev-build.sh on mac for local build, you need to have install4j installed
    elif [ $(uname) = "Darwin" ]; then
        ./gradlew -Pdevbuild=true -PgradlewInstall4jDirectory=/Applications/install4j.app :jvm:workbookapp:install4jdeploy ;
# ./dev-build.sh on linux for local build, you need to have install4j installed
    else
        ./gradlew -Pdevbuild=true -PgradlewInstall4jDirectory=/opt/install4j :jvm:workbookapp:install4jdeploy
fi