#!/bin/bash

SLUG="TheMrMilchmann/MJLOptions"
JDK="oraclejdk8"
BRANCH="master"

set -e
./gradlew check --info -S --parallel -Psnapshot

if [ "$TRAVIS_REPO_SLUG" == "$SLUG" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
    if [ "$TRAVIS_JDK_VERSION" == "$JDK" ] && [ "$TRAVIS_BRANCH" == "$BRANCH" ]; then
        # Upload snapshot artifacts to OSSRH.
        echo -e "[deploy.sh] Publishing snapshots...\n"
        ./gradlew publish --parallel -Psnapshot
        echo -e "[deploy.sh] Published snapshots to OSSRH.\n"
    fi
fi