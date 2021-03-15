package com.github.zerokode.testobjects;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * This class will allow you to read and convert the JSON or YAML files you write to represent objects as instances
 * that you can use in your Unit/Integration tests.
 *
 */
public class TestObjectsReader {

    private final ObjectMapper objectMapper;
    private static final Logger log = Logger.getLogger(TestObjectsReader.class);

    public TestObjectsReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Creates a POJO by loading the file located at the src/test/resources/ folder into an object of the given type.
     *
     * @param jsonFileName - just the file name, the path will always be the one indicated above.
     * @param clazz        - the class of this object
     * @param <T>          - the type of this object
     * @return - an instance of type T
     */
    public <T> T read(final String jsonFileName, Class<T> clazz) {
        log.info("Reading Test Object from file " + jsonFileName);
        try {
            final String jsonString = loadFromClassPath(jsonFileName, clazz);
            return objectMapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("Something went wrong when reading or parsing JSON test object", e);
        }
    }

    /**
     * Reads and returns the contents of the given file. The file is read from the classpath.
     * The file name should be relative to the classpath (src/test/resources folder).
     *
     * @param jsonFileName - the file name only, relative to "src/test/resources/"
     * @param clazz        - the class from which we will get the ClassLoader
     * @return - the file contents as plain text
     */
    private String loadFromClassPath(String jsonFileName, Class<?> clazz) {
        try {
            InputStream is = clazz.getClassLoader().getResourceAsStream(jsonFileName);
            assert is != null;
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Test Object file called " + jsonFileName + " not found at path " + jsonFileName, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Test Objects from file '" + jsonFileName + "'", e);
        }
    }

}
