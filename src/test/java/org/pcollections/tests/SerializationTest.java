package org.pcollections.tests;

import java.io.*;
import java.util.Arrays;
import junit.framework.TestCase;
import org.pcollections.*;

/**
 * Tests related to serialization / deserialization.
 *
 * @author matei
 */
public class SerializationTest extends TestCase {

  private static final String ELEMENT1 = "ELEMENT001";
  private static final String ELEMENT2 = "ELEMENT002";
  private static final String ELEMENT3 = "ELEMENT003";

  public void testSerializationForHashPMap() {
    final HashPMap<String, Long> initialHashPMap =
        HashTreePMap.<String, Long>empty()
            .plus("key001", 23L)
            .plus("key002", 29L)
            .plus("key003", 31L);
    final byte[] serializedThirdPHashPMap = serializeToByteArray(initialHashPMap);

    assertNotNull(serializedThirdPHashPMap);
    assertTrue(serializedThirdPHashPMap.length > 0);

    @SuppressWarnings("unchecked")
    final HashPMap<String, Long> deserializedHashPMap =
        (HashPMap<String, Long>) deserializeFromByteArray(serializedThirdPHashPMap, HashPMap.class);

    assertNotNull(deserializedHashPMap);
    assertEquals(Long.valueOf(23L), deserializedHashPMap.get("key001"));
    assertEquals(Long.valueOf(29L), deserializedHashPMap.get("key002"));
    assertEquals(Long.valueOf(31L), deserializedHashPMap.get("key003"));

    assertNotSame(initialHashPMap, deserializedHashPMap);
    assertEquals(initialHashPMap, deserializedHashPMap);
  }

  public void testSerializationForHashPMapWithEntrySet() {
    /* regression test because entry set cache is unserializable, but wasn't marked as transient */
    final HashPMap<String, Long> initialHashPMap =
        HashTreePMap.<String, Long>empty()
            .plus("key001", 23L)
            .plus("key002", 29L)
            .plus("key003", 31L);
    initialHashPMap.entrySet();
    serializeToByteArray(initialHashPMap); // used to throw an exception
  }

  public void testSerializationForAmortizedPQueue() {
    final AmortizedPQueue<String> initialPQueue =
        AmortizedPQueue.<String>empty().plus(ELEMENT1).plus(ELEMENT2).plus(ELEMENT3);
    final byte[] serializedPQueue = serializeToByteArray(initialPQueue);

    assertNotNull(serializedPQueue);
    assertTrue(serializedPQueue.length > 0);

    @SuppressWarnings("unchecked")
    final AmortizedPQueue<String> deserializedPQueue =
        deserializeFromByteArray(serializedPQueue, AmortizedPQueue.class);

    assertNotNull(deserializedPQueue);
    assertEquals(3, deserializedPQueue.size());
    assertNotSame(initialPQueue, deserializedPQueue);

    Object[] deserializedElements = deserializedPQueue.toArray();
    assertEquals(ELEMENT1, deserializedElements[0]);
    assertEquals(ELEMENT2, deserializedElements[1]);
    assertEquals(ELEMENT3, deserializedElements[2]);
  }

  public void testSerializationForConsPStack() {
    final ConsPStack<String> initialConsPStack =
        ConsPStack.from(Arrays.asList(ELEMENT1, ELEMENT2, ELEMENT3));
    final byte[] serializedConsPStack = serializeToByteArray(initialConsPStack);

    assertNotNull(serializedConsPStack);
    assertTrue(serializedConsPStack.length > 0);

    @SuppressWarnings("unchecked")
    final ConsPStack<String> deserializedConsPStack =
        deserializeFromByteArray(serializedConsPStack, ConsPStack.class);

    assertNotNull(deserializedConsPStack);
    assertEquals(3, deserializedConsPStack.size());
    assertNotSame(initialConsPStack, deserializedConsPStack);

    assertEquals(ELEMENT1, deserializedConsPStack.get(0));
    assertEquals(ELEMENT2, deserializedConsPStack.get(1));
    assertEquals(ELEMENT3, deserializedConsPStack.get(2));
  }

  public void testSerializationForMapPBag() {
    final MapPBag<String> initialMapPBag =
        HashTreePBag.<String>empty()
            .plus(ELEMENT1)
            .plus(ELEMENT2)
            .plus(ELEMENT1)
            .plus(ELEMENT2)
            .plus(ELEMENT1)
            .plus(ELEMENT3);
    final byte[] serializedMapPBag = serializeToByteArray(initialMapPBag);

    assertNotNull(serializedMapPBag);
    assertTrue(serializedMapPBag.length > 0);

    @SuppressWarnings("unchecked")
    final MapPBag<String> deserializedMapPBag =
        deserializeFromByteArray(serializedMapPBag, MapPBag.class);

    assertNotNull(deserializedMapPBag);
    assertEquals(6, deserializedMapPBag.size());
    assertNotSame(initialMapPBag, deserializedMapPBag);

    assertTrue(deserializedMapPBag.containsAll(Arrays.asList(ELEMENT1, ELEMENT2, ELEMENT3)));
    assertFalse(
        deserializedMapPBag.minus(ELEMENT1).minus(ELEMENT1).minus(ELEMENT1).contains(ELEMENT1));
    assertFalse(deserializedMapPBag.minus(ELEMENT2).minus(ELEMENT2).contains(ELEMENT2));
    assertFalse(deserializedMapPBag.minus(ELEMENT3).contains(ELEMENT3));
  }

  public void testSerializationForMapPSet() {
    final MapPSet<String> initialMapPSet =
        HashTreePSet.<String>empty().plus(ELEMENT1).plus(ELEMENT2).plus(ELEMENT3);
    final byte[] serializedMapPSet = serializeToByteArray(initialMapPSet);

    assertNotNull(serializedMapPSet);
    assertTrue(serializedMapPSet.length > 0);

    @SuppressWarnings("unchecked")
    final MapPSet<String> deserializedMapPSet =
        deserializeFromByteArray(serializedMapPSet, MapPSet.class);

    assertNotNull(deserializedMapPSet);
    assertEquals(3, deserializedMapPSet.size());
    assertNotSame(initialMapPSet, deserializedMapPSet);

    assertTrue(deserializedMapPSet.containsAll(Arrays.asList(ELEMENT1, ELEMENT2, ELEMENT3)));
    assertTrue(deserializedMapPSet.minus(ELEMENT1).minus(ELEMENT2).minus(ELEMENT3).isEmpty());
  }

  public void testSerializationForOrderedPSet() {
    final OrderedPSet<String> initialOrderedPSet =
        OrderedPSet.from(Arrays.asList(ELEMENT1, ELEMENT2, ELEMENT3));
    final byte[] serializedOrderedPSet = serializeToByteArray(initialOrderedPSet);

    assertNotNull(serializedOrderedPSet);
    assertTrue(serializedOrderedPSet.length > 0);

    @SuppressWarnings("unchecked")
    final OrderedPSet<String> deserializedOrderedPSet =
        deserializeFromByteArray(serializedOrderedPSet, OrderedPSet.class);

    assertNotNull(deserializedOrderedPSet);
    assertEquals(3, deserializedOrderedPSet.size());
    assertNotSame(initialOrderedPSet, deserializedOrderedPSet);

    assertEquals(0, deserializedOrderedPSet.indexOf(ELEMENT1));
    assertEquals(1, deserializedOrderedPSet.indexOf(ELEMENT2));
    assertEquals(2, deserializedOrderedPSet.indexOf(ELEMENT3));
  }

  public void testSerializationForTreePVector() {
    final TreePVector<String> initialTreePVector =
        TreePVector.from(Arrays.asList(ELEMENT1, ELEMENT2, ELEMENT3));
    final byte[] serializedTreePVector = serializeToByteArray(initialTreePVector);

    assertNotNull(serializedTreePVector);
    assertTrue(serializedTreePVector.length > 0);

    @SuppressWarnings("unchecked")
    final TreePVector<String> deserializedTreePVector =
        deserializeFromByteArray(serializedTreePVector, TreePVector.class);

    assertNotNull(deserializedTreePVector);
    assertEquals(3, deserializedTreePVector.size());
    assertNotSame(initialTreePVector, deserializedTreePVector);

    assertEquals(0, deserializedTreePVector.indexOf(ELEMENT1));
    assertEquals(1, deserializedTreePVector.indexOf(ELEMENT2));
    assertEquals(2, deserializedTreePVector.indexOf(ELEMENT3));
  }

  private static <SERIALIZABLE extends Serializable> byte[] serializeToByteArray(
      final SERIALIZABLE objectToSerialize) {
    try {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

      objectOutputStream.writeObject(objectToSerialize);
      objectOutputStream.close();

      return byteArrayOutputStream.toByteArray();
    } catch (final IOException e) {
      throw new RuntimeException("Could not serialize input object to byte array", e);
    }
  }

  private static <SERIALIZABLE extends Serializable> SERIALIZABLE deserializeFromByteArray(
      final byte[] bytes, Class<SERIALIZABLE> targetClass) {
    try {
      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
      ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

      Object deserializedObject = objectInputStream.readObject();
      return targetClass.cast(deserializedObject);
    } catch (final IOException ioException) {
      throw new RuntimeException("Could not deserialize input byte array", ioException);
    } catch (final ClassNotFoundException classNotFoundException) {
      throw new RuntimeException("Could not deserialize input byte array", classNotFoundException);
    }
  }
}
