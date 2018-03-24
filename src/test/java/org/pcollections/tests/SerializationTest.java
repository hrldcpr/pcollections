package org.pcollections.tests;

import junit.framework.TestCase;
import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;

import java.io.*;

/**
 * Tests related to serialization / deserialization.
 *
 * @author matei
 */
public class SerializationTest extends TestCase {

    public void testSerializationForHashPMap() {
        final HashPMap<String, Long> emptyHashPMap = HashTreePMap.<String, Long> empty();
        final HashPMap<String, Long> firstHashPMap = emptyHashPMap.plus("key001", 23L);
        final HashPMap<String, Long> secondHashPMap = firstHashPMap.plus("key002", 29L);
        final HashPMap<String, Long> thirdHashPMap = secondHashPMap.plus("key003", 31L);

        final byte[] serializedThirdPHashPMap = serializeToByteArray(thirdHashPMap);

        assertNotNull(serializedThirdPHashPMap);
        assertTrue(serializedThirdPHashPMap.length > 0);

        @SuppressWarnings("unchecked")
        HashPMap<String, Long> deserializedHashPMap =
                (HashPMap<String, Long>) deserializeFromByteArray(serializedThirdPHashPMap, HashPMap.class);

        assertNotNull(deserializedHashPMap);
        assertEquals(Long.valueOf(23L), deserializedHashPMap.get("key001"));
        assertEquals(Long.valueOf(29L), deserializedHashPMap.get("key002"));
        assertEquals(Long.valueOf(31L), deserializedHashPMap.get("key003"));

        assertNotSame(thirdHashPMap, deserializedHashPMap);
        assertEquals(thirdHashPMap, deserializedHashPMap);
        assertFalse(deserializedHashPMap.equals(secondHashPMap));
        assertFalse(deserializedHashPMap.equals(firstHashPMap));
    }

    private static <SERIALIZABLE extends Serializable> byte[] serializeToByteArray(final SERIALIZABLE objectToSerialize) {
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

    private static <SERIALIZABLE extends Serializable> SERIALIZABLE deserializeFromByteArray(final byte[] bytes, Class<SERIALIZABLE> targetClass) {
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
