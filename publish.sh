#!/usr/bin/env bash

set -e

echo "* [core] publish *"
cd microdocs-core-java
./gradlew clean publish --warning-mode all --stacktrace

echo "* [doclet] build *"
cd ../microdocs-crawler-doclet
./gradlew clean fatJar --warning-mode all --stacktrace

echo "* [plugin] clean *"
cd ../microdocs-crawler-gradle
./gradlew clean --warning-mode all --stacktrace

echo "* [plugin] 'include' doclet *"
cp ../microdocs-crawler-doclet/build/libs/microdocs-crawler-doclet-all-*.jar src/main/resources/microdocs-crawler-doclet.jar

echo "* [plugin] publish *"
./gradlew publish --warning-mode all --stacktrace
