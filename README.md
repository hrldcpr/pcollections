PCollections
============

A Persistent Java Collections Library

[![Maven Central](https://img.shields.io/maven-central/v/org.pcollections/pcollections.svg)](https://mvnrepository.com/artifact/org.pcollections/pcollections/latest)
[![Javadoc](https://www.javadoc.io/badge/org.pcollections/pcollections.svg)](https://www.javadoc.io/doc/org.pcollections/pcollections)

### Overview

PCollections serves as a [persistent](https://en.wikipedia.org/wiki/Persistent_data_structure) and immutable analogue of the Java Collections Framework. This includes **efficient**, **thread-safe**, **generic**, **immutable**, and **persistent** stacks, maps, vectors, sets, and bags, **compatible** with their Java Collections counterparts.

Persistent and immutable datatypes are increasingly appreciated as a **simple**, **design-friendly**, **concurrency-friendly**, and sometimes more time- and space-efficient alternative to mutable datatypes.

### Persistent versus Unmodifiable

Note that these immutable collections are very different from the immutable collections returned by Java's [Collections.unmodifiableCollection()](https://docs.oracle.com/javase/8/docs/api/java/util/Collections.html#unmodifiableCollection-java.util.Collection-) and similar methods. The difference is that Java's unmodifiable collections have no producers, whereas PCollections have very efficient producers.

Thus if you have an unmodifiable Collection `x` and you want a new Collection `x2` consisting of the elements of `x` in addition to some element `e`, you would have to do something like:
```Java
Collection x2 = new HashSet(x);
x2.add(e);
```
which involves copying all of `x`, using linear time and space.

If, on the other hand, you have a PCollection `y` you can simply say:
```Java
PCollection y2 = y.plus(e);
```
which still leaves `y` untouched but generally requires little or no copying, using time and space much more efficiently.

### Usage

PCollections are created using producers and static factory methods. Some example static factory methods are [`HashTreePSet.empty()`](https://javadoc.io/page/org.pcollections/pcollections/latest/org/pcollections/HashTreePSet.html#empty()) which returns an empty [PSet](https://javadoc.io/page/org.pcollections/pcollections/latest/org/pcollections/PSet.html), while `HashTreePSet.singleton(e)` returns a PSet containing just the element `e`, and `HashTreePSet.from(collection)` returns a PSet containing the same elements as `collection`. See [Example Code](#example-code) below for an example of using producers.

The same `empty()`, `singleton()`, and `from()` factory methods are found in each of the PCollections implementations, which currently include one concrete implementation for each abstract type:
* [HashTreePMap](https://javadoc.io/page/org.pcollections/pcollections/latest/org/pcollections/HashTreePMap.html) provides a [PMap](https://javadoc.io/page/org.pcollections/pcollections/latest/org/pcollections/PMap.html) implementation, analogous to Java's HashMap.
* [ConsPStack](https://javadoc.io/page/org.pcollections/pcollections/latest/org/pcollections/ConsPStack.html) provides a [PStack](https://javadoc.io/page/org.pcollections/pcollections/latest/org/pcollections/PStack.html) implementation, analogous to Java's LinkedList.
* [TreePVector](https://javadoc.io/page/org.pcollections/pcollections/latest/org/pcollections/TreePVector.html) provides a [PVector](https://javadoc.io/page/org.pcollections/pcollections/latest/org/pcollections/PVector.html) implementation, analogous to Java's ArrayList.
* [HashTreePSet](https://javadoc.io/page/org.pcollections/pcollections/latest/org/pcollections/HashTreePSet.html) provides a [PSet](https://javadoc.io/page/org.pcollections/pcollections/latest/org/pcollections/PSet.html) implementation, analogous to Java's HashSet.
* [HashTreePBag](https://javadoc.io/page/org.pcollections/pcollections/latest/org/pcollections/HashTreePBag.html) provides a [PBag](https://javadoc.io/page/org.pcollections/pcollections/latest/org/pcollections/PBag.html) implementation, which is unordered like a set but can contain duplicate elements.

PCollections are highly interoperable with Java Collections: every PCollection is a java.util.Collection, every PMap is a java.util.Map, every PSequence — including every PStack and PVector — is a java.util.List, and every PSet is a java.util.Set.

PCollections uses [Semantic Versioning](https://semver.org/), which establishes a strong correspondence between API changes and version numbering.

PCollections is in the [Maven Central repository](https://search.maven.org/#search|ga|1|a:%22pcollections%22), under org.pcollections. Thus the Maven coordinates for PCollections are:

```xml
<dependency>
    <groupId>org.pcollections</groupId>
    <artifactId>pcollections</artifactId>
    <version>3.1.4</version>
</dependency>
```

or Gradle:
```groovy
compile 'org.pcollections:pcollections:3.1.4'
```

### Example Code

The following gives a very simple example of using PCollections, including the static factory method HashTreePSet.empty() and the producer plus(e):
```Java
import org.pcollections.*;

public class Example {
  public static void main(String... args) {
    PSet<String> set = HashTreePSet.empty();
    set = set.plus("something");

    System.out.println(set);
    System.out.println(set.plus("something else"));
    System.out.println(set);
  }
}
```
Running this program gives the following output:
```
[something]
[something else, something]
[something]
```

### Building form source
To build the project from source [clone the repository](https://github.com/pcollections/pcollections.git) and then run `./gradlew`

### Related Work

[Clojure](https://clojure.org/) and [Scala](https://www.scala-lang.org/) also provide persistent collections on the JVM, but they are less interoperable with Java. Both [Guava](https://github.com/google/guava) and [`java.util.Collections`](https://docs.oracle.com/javase/8/docs/api/java/util/Collections.html#unmodifiableCollection-java.util.Collection-) provide immutable collections but they are not persistent—that is, they do not provide efficient producers—so they are not nearly as useful. See [Persistent versus Unmodifiable](#persistent-versus-unmodifiable) above.
