package com.hedwig.morpheus.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by hugo. All rights reserved.
 */

@Component
@Scope("singleton")
public class JSONUtilities {

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
    }

    public static String serialize(Object object) throws JsonProcessingException {
        String jsonString = mapper.writeValueAsString(object);
        return jsonString;
    }

    public static <T> T deserialize(String jsonString, Class<T> clazz) throws IOException {
        T tObject = mapper.readValue(jsonString, clazz);
        return tObject;
    }

    public static <T> T deserialize(String jsonString, TypeReference valueTypeRef) throws IOException {
        T tObject = mapper.readValue(jsonString, valueTypeRef);
        return tObject;
    }
}
