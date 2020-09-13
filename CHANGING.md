First time setup:
- create a `gradle.properties` a la https://central.sonatype.org/pages/gradle.html

To release:
- run `java -jar google-java-format-1.7-all-deps.jar -i src/**.java`
- remove -SNAPSHOT from the version in build.gradle
- commit with a tag
- run `./gradlew test uploadArchives`

Then follow the instructions at https://central.sonatype.org/pages/releasing-the-deployment.html

Finally, increment the version in build.gradle and add back -SNAPSHOT, and commit.

Finally finally, once the new version is available in Maven Central (takes a few hours), update the version in the Maven and Gradle snippets in the README and update CHANGELOG.md.
