#!/usr/bin/env bash

# publish core
echo "* publish core *"
cd microdocs-core-java
./gradlew uploadArchives

# build doclet
echo "* build doclet *"
cd ../microdocs-crawler-doclet
./gradlew fatJar

# copy doclet
echo "* copy doclet *"
rm -f ../microdocs-crawler-gradle/src/main/resources/microdocs-crawler-doclet.jar
cp build/libs/microdocs-crawler-doclet-all-1.8.0.jar ../microdocs-crawler-gradle/src/main/resources/microdocs-crawler-doclet.jar

# publish gradle plugin
echo "* publish gradle plugin *"
cd ../microdocs-crawler-gradle
./gradlew uploadArchives