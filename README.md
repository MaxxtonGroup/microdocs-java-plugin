# microdocs-java-plugin
This is the Java plugin to publish data to your [microdocs server](https://github.com/MaxxtonGroup/microdocs). For more info, see the three READMEs's in the subfolders.


## Publishing

Artifacts publish to Maxxton's internal Maven registry, not Maven Central - no GPG signing or Sonatype account needed.

`microdocs-crawler-gradle` and `microdocs-core-java` use the `com.maxxton.convention` plugin, which configures the publication and registry automatically. `microdocs-crawler-doclet` never publishes its own artifact - only its fat jar is bundled into `microdocs-crawler-gradle`'s published jar.

To publish a new version:
1. Bump `version` in `gradle.properties` for whichever module(s) changed.
2. Run:
```
$ ./publish.sh
```

### Java 8
Checkout the java8 branch for a Java 8 compatible version. The master branch is based on Java 21.

