First time setup:

- figure out Maven Central login and GPG keys, good luck with that...
- download `google-java-format-1.21.0-all-deps.jar` from https://github.com/google/google-java-format/releases

To release:

- `java -jar google-java-format-1.21.0-all-deps.jar --replace src/**.java` (note this uses Fish Shell syntax, you'll need to do something else in other shells)
- `./gradlew test`
- remove -SNAPSHOT from the version in build.gradle
- commit with a tag
- `./gradlew publish`
- `cd build/repo && tar cvzf archive.tgz org/`
- go to https://central.sonatype.com/ and log in and click Publish Component
- for Deployment Name use "org.pcollections:pcollections:4.3.2" (replace with actual version)
- upload archive.tgz, and publish it once it's verified

Finally, increment the version in build.gradle and add back -SNAPSHOT, and commit.

Finally finally, once the new version is available in Maven Central (takes a few hours), update the version in the Maven and Gradle snippets in the README and update CHANGELOG.md.

…also, if you want to run the benchmarks:
(They're a bit janky and take forever, but you can always add your own and comment out the ones you don't want to run.)

- `./gradlew --stop; rm -fr build/ && ./gradlew jmh`
  (Stopping the daemon and deleting build/ fixes issues when you're changing or commenting out benchmarks.)
