// FILE 21: SerializationUtil.java
package com.eduplatform.cache.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SerializationUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * SERIALIZE TO JSON
     */
    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Serialization failed", e);
        }
    }

    /**
     * DESERIALIZE FROM JSON
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }
}