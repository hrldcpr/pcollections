PCollections
============

A Persistent Java Collections Library

[![Maven Central](https://img.shields.io/maven-central/v/org.pcollections/pcollections.svg)](https://maven-badges.herokuapp.com/maven-central/org.pcollections/pcollections/)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/org.pcollections/pcollections/badge.svg)](http://www.javadoc.io/doc/org.pcollections/pcollections)

###Overview

PCollections serves as a [persistent](http://en.wikipedia.org/wiki/Persistent_data_structure) and immutable analogue of the [Java Collections Framework](http://java.sun.com/javase/6/docs/technotes/guides/collections/index.html). This includes **efficient**, **thread-safe**, **generic**, **immutable**, and **persistent** stacks, maps, vectors, sets, and bags, **compatible** with their Java Collections counterparts.

Persistent and immutable datatypes are increasingly appreciated as a **simple**, **design-friendly**, **concurrency-friendly**, and sometimes more time- and space-efficient alternative to mutable datatypes.
###Persistent versus Unmodifiable

Note that these immutable collections are very different from the immutable collections returned by Java's [Collections.unmodifiableCollection()](http://java.sun.com/javase/6/docs/api/java/util/Collections.html#unmodifiableCollection(java.util.Collection)) and similar methods. The difference is that Java's unmodifiable collections have no producers, whereas PCollections have very efficient producers. Thus if you have an unmodifiable Collection x and you want a new Collection x2 consisting of the elements of x in addition to some element e, you would have to do something like:
```Java
Collection x2 = new HashSet(x);
x2.add(e);
```
which involves copying all of x, using linear time and space. If, on the other hand, you have a PCollection y you can simply say:
```Java
PCollection y2 = y.plus(e);
```
which still leaves y untouched but generally requires little or no copying, using time and space much more efficiently.
###Usage

PCollections are created using producers and static factory methods. Some example static factory methods are [HashTreePSet.empty()](http://pcollections.googlecode.com/svn/trunk/docs/org/pcollections/HashTreePSet.html#empty()) which returns an empty [PSet](http://pcollections.googlecode.com/svn/trunk/docs/org/pcollections/PSet.html), while HashTreePSet.singleton(e) returns a PSet containing just the element e, and HashTreePSet.from(collection) returns a PSet containing the same elements as collection. See 'Example Code' below for an example of using producers.

The same empty(), singleton(), and from() factory methods are found in each of the PCollections implementations, which currently include one concrete implementation for each abstract type:
* [HashTreePMap](http://pcollections.googlecode.com/svn/trunk/docs/org/pcollections/HashTreePMap.html) provides a [PMap](http://pcollections.googlecode.com/svn/trunk/docs/org/pcollections/PMap.html) implementation, analogous to Java's HashMap.
* [ConsPStack](http://pcollections.googlecode.com/svn/trunk/docs/org/pcollections/ConsPStack.html) provides a [PStack](http://pcollections.googlecode.com/svn/trunk/docs/org/pcollections/PStack.html) implementation, analogous to Java's LinkedList.
* [TreePVector](http://pcollections.googlecode.com/svn/trunk/docs/org/pcollections/TreePVector.html) provides a [PVector](http://pcollections.googlecode.com/svn/trunk/docs/org/pcollections/PVector.html) implementation, analogous to Java's ArrayList.
* [HashTreePSet](http://pcollections.googlecode.com/svn/trunk/docs/org/pcollections/HashTreePSet.html) provides a [PSet](http://pcollections.googlecode.com/svn/trunk/docs/org/pcollections/PSet.html) implementation, analogous to Java's HashSet.
* [HashTreePBag](http://pcollections.googlecode.com/svn/trunk/docs/org/pcollections/HashTreePBag.html) provides a [PBag](http://pcollections.googlecode.com/svn/trunk/docs/org/pcollections/PBag.html) implementation, which is unordered like a set but can contain duplicate elements. 

PCollections are highly interoperable with Java Collections: every PCollection is a java.util.Collection, every PMap is a java.util.Map, every PSequence — including every PStack and PVector — is a java.util.List, and every PSet is a java.util.Set.

PCollections uses [Semantic Versioning](http://semver.org/), which establishes a strong correspondence between API changes and version numbering.

PCollections is in the [Maven Central repository](http://search.maven.org/#search|ga|1|a:%22pcollections%22), under org.pcollections. Thus the Maven coordinates for PCollections are:

```xml
<dependency>
    <groupId>org.pcollections</groupId>
    <artifactId>pcollections</artifactId>
    <version>2.1.2</version>
</dependency>
```

###Example Code

The following gives a very simple example of using PCollections, including the static factory method HashTreePSet.empty() and the producer plus(e):
```Java
import pcollections.*;
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

###Building form source
For building the project from source [clone the repository](https://github.com/pcollections/pcollections.git) and then execute
```
 ./gradlew build
```
This will compile all files, execute the tests and create a jar in ./build/libs. usefull targets are:
* jar - to create the jar
* clean - to clean all build files, including the jar
* test - to build the project and run the test files
* compileJava - to only compile the Java files

###Related Work

[Clojure](http://clojure.googlecode.com/) also provides persistent collections in Java, but for now they are less interoperable with Java Collections, and seem more designed to be used within the Clojure language itself. Both [Guava](http://guava-libraries.googlecode.com/) and Java's [Collections](http://java.sun.com/javase/6/docs/api/java/util/Collections.html) utility class provide immutable collections but they are not persistent, that is they do not provide efficient producers, so they are not nearly as useful. See [Persistent versus Unmodifiable](#persistent-versus-unmodifiable) above. 
