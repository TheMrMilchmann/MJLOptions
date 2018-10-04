#!/bin/bash

SLUG="TheMrMilchmann/MJLOptions"
JDK="oraclejdk8"
JDOC_JDK="openjdk11"
BRANCH="master"

set -e

if [ "$TRAVIS_REPO_SLUG" == "$SLUG" ] && [ "$TRAVIS_JDK_VERSION" == "$JDK" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "$BRANCH" ]; then
    if [ "$TRAVIS_JDK_VERSION" == "$JDK" ]; then
        # Upload snapshot artifacts to OSSRH.
        echo -e "[deploy.sh] Publishing snapshots...\n"
        ./gradlew publish --parallel -Psnapshot
        echo -e "[deploy.sh] Published snapshots to OSSRH.\n"
    fi

    if [ "$TRAVIS_JDK_VERSION" == "$JDOC_JDK" ]; then
        # Upload JavaDoc to GH-Pages.
        echo -e "[deploy.sh] Publishing documentation...\n"
        mkdir out
        cd out
        git init
        git config user.name "Deployment Bot"
        git config user.email "deploy@travis-ci.org"

        if [ "$TRAVIS_TAG" =~ ^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(-(0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(\.(0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*)?(\+[0-9a-zA-Z-]+(\.[0-9a-zA-Z-]+)*)?$ ]; then
            cp -r ../build/docs/javadoc/* ./docs/${TRAVIS_TAG}
            COMMIT_MSG="feat(ci): $TRAVIS_TAG release documentation"
        else
            cp -r ../build/docs/javadoc/* ./docs/snapshot/
            COMMIT_MSG="feat(ci): snapshot documentation for build $TRAVIS_BUILD_NUMBER ($TRAVIS_COMMIT)"
        fi

        git add .
        git commit -m "$COMMIT_MSG"
        git push --quiet "https://${GH_TOKEN}@github.com/$SLUG" master:gh-pages
        echo -e "[deploy.sh] Published documentation.\n"
    fi
fi