# MicroDocs Gradle plugin
Gradle plugin for generating definitions using the [MicroDocsCrawler Doclet](../microdocs-crawler-doclet).

## Setup

build.gradle
```
buildscript {
  mavenCentral()
  dependencies {
    classpath('com.maxxton:microdocs-crawler-gradle:1.0')
    classpath ("com.fasterxml.jackson.core:jackson-databind:2.7.5")
    classpath ("com.mashape.unirest:unirest-java:1.4.9")
    classpath ("com.maxxton.microdocs:microdocs-core-java:1.0")
  }
}
apply plugin: 'microdocs'

buildMicroDocs{
  options.addStringOption("group", "services")
}

checkMicroDocs{
  reportFile = 'build/reports/microdocs.json';
  url = "http://microdocs-server";
}

publishMicroDocs{
  reportFile = 'build/reports/microdocs.json';
  url = "http://microdocs-server";
  group = "services";
  failOnProblems = true;
}
```

## Usage
* `gradle microDocs` - Generate definitions
* `gradle checkMicroDocs` - Check new definitions against the MicroDocs server
* `gradle publishMicroDocs` - Publish definitions to the MicroDocs server

## Build
```
# Assuming your start from the README's directory
cd ../microdocs-crawler-doclet
./gradlew fatJar
cd -
cp ../microdocs-crawler-doclet/build/libs/microdocs-crawler-doclet.jar src/main/resources/microdocs-crawler-doclet.jar
./gradlew build
```
