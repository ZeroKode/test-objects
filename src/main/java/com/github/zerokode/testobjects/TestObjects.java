package com.github.zerokode.testobjects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.google.common.base.CaseFormat;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * This class will do mainly two things:
 * <p>
 * - Allow you to create JSON schemas for any class you want and save those schema files in your project.
 * Schemas will help you type json files that represent object instances with code autocomplete functionality (requires some IDE setup).
 * <p>
 * - Read and convert the JSON files you wrote as instances that you can use in your Unit/Integration tests.
 * <p>
 * JSON Schemas are automatically saved into the src/test/resources/schemas folder.
 * Test Objects must be placed in the src/test/resources/objects folder.
 */
public class TestObjects {

    private final ObjectMapper objectMapper;
    private static final Logger log = Logger.getLogger(TestObjects.class);

    public static final String SCHEMAS_SUB_FOLDER = "schemas";
    public static final String SRC_TEST_RESOURCES_SCHEMAS = "/src/test/resources/" + SCHEMAS_SUB_FOLDER;

    public TestObjects(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private void howToSetupJsonSchemas(final String className, final String moduleName) {
        log.info("How to setup this JSON schema in IntelliJ IDEA: \n");

        log.info("1. Go to settings > Languages & Frameworks > Schemas and DTOs > JSON Schema Mappings");
        log.info("2. Add a new schema using the " + moduleName + "/" + SRC_TEST_RESOURCES_SCHEMAS + "/" + className + ".json schema file");
        log.info("3. Set file name patterns for that schema like: " + className + "*.json and " + toKebabCase(className) + "*.json\n");

        log.info("Now you can start writing JSON files with auto completion enabled.");
    }

    private String toKebabCase(String className) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, className);
    }

    /**
     * Creates a POJO by loading the file located at the src/test/resources/ folder into an object of the given type.
     *
     * @param jsonFileName - just the file name, the path will always be the one indicated above.
     * @param clazz        - the class of this object
     * @param <T>          - the type of this object
     * @return - an instance of type T
     */
    public <T> T loadTestObject(final String jsonFileName, Class<T> clazz) {
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

    /**
     * Creates a JSON Schema file and saves it at the src/test/resources/schemas folder of the root module.
     *
     * @param clazz - The class from which we want the schema to be generated
     * @throws JsonProcessingException - when for some reason a JSON schema cannot be created
     */
    public void generateAndSaveJSONSchema(final Class<?> clazz) throws JsonProcessingException {
        generateAndSaveJSONSchema(clazz, "");
    }

    /**
     * Creates a JSON Schema file and saves it at the src/test/resources/schemas folder of the specified java submodule.
     *
     * @param clazz      - The class from which we want the schema to be generated
     * @param moduleName - the name of the java submodule where the schema file will be saved (e.g. "Model", "SomeService", etc).
     * @throws JsonProcessingException - when for some reason a JSON schema cannot be created
     */
    public void generateAndSaveJSONSchema(final Class<?> clazz, final String moduleName) throws JsonProcessingException {
        verifyModuleFolder(moduleName);
        final String jsonSchema = generateSchemaAsJsonString(clazz);
        final String targetFolder = moduleName + SRC_TEST_RESOURCES_SCHEMAS;
        createFolder(targetFolder);
        final String destination = targetFolder + "/" + clazz.getSimpleName() + ".json";

        try (FileWriter fileWriter = new FileWriter(destination)) {
            fileWriter.write(jsonSchema);
            howToSetupJsonSchemas(clazz.getSimpleName(), moduleName);
        } catch (IOException e) {
            log.error("Failed to save json schema file at: " + destination, e);
        }
    }

    private String generateSchemaAsJsonString(Class<?> clazz) throws JsonProcessingException {
        JsonSchemaGenerator generator = new JsonSchemaGenerator(objectMapper);
        JsonSchema jsonSchema = generator.generateSchema(clazz);
        return objectMapper.writeValueAsString(jsonSchema);
    }

    private void verifyModuleFolder(String moduleName) {
        File folder = new File(moduleName);
        if (!folder.exists()) {
            throw new IllegalArgumentException("No such module " + moduleName);
        }
    }

    private void createFolder(String targetFolder) {
        File folder = new File(targetFolder);
        if (!folder.exists()) {
            boolean created = folder.mkdir();
            if (created)
                log.info("Created folder " + targetFolder);
        }
    }

}
