First time setup:

- **TODO** do we need to generate signing keys for Sonatype?
- download `google-java-format-1.21.0-all-deps.jar` from https://github.com/google/google-java-format/releases

To release:

- run `java -jar google-java-format-1.21.0-all-deps.jar --replace src/**.java` (note this uses Fish Shell syntax, you'll need to do something else in other shells)
- remove -SNAPSHOT from the version in build.gradle
- commit with a tag
- run `./gradlew test publish`

Then follow the instructions at https://central.sonatype.org/publish/publish-portal-upload/

Finally, increment the version in build.gradle and add back -SNAPSHOT, and commit.

Finally finally, once the new version is available in Maven Central (takes a few hours), update the version in the Maven and Gradle snippets in the README and update CHANGELOG.md.

…also, if you want to run the benchmarks:
(They're a bit janky and take forever, but you can always add your own and comment out the ones you don't want to run.)

- `./gradlew --stop; rm -fr build/ && ./gradlew jmh`
  (Stopping the daemon and deleting build/ fixes issues when you're changing or commenting out benchmarks.)
