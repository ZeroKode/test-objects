package com.github.zerokode.testobjects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.google.common.base.CaseFormat;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class will allow you to create JSON Schema files based on a given class. The generated schemas are
 * automatically saved into the src/test/resources/schemas folder. When a module is indicated then the schema(s)
 * will be saved into the src/test/resources/schemas folder of that module.
 *
 * JSON Schemas will allow you to write JSON and YAML files with auto completion using an IDE. This requires some
 * additional IDE setup that consists in associating a JSON Schema file with files that match some file name pattern
 * like: user-*.json, user-*.yaml, user-*.yml, User*.json or any other patterns you define.
 */
public class TestObjectsSchemaGenerator {

    private final ObjectMapper objectMapper;
    private static final Logger log = Logger.getLogger(TestObjectsSchemaGenerator.class);

    public static final String SCHEMAS_SUB_FOLDER = "schemas";
    public static final String SRC_TEST_RESOURCES_SCHEMAS = "/src/test/resources/" + SCHEMAS_SUB_FOLDER;

    public TestObjectsSchemaGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Creates a JSON Schema file and saves it at the src/test/resources/schemas folder of the root module.
     *
     * @param clazz - The class from which we want the schema to be generated
     * @throws JsonProcessingException - when for some reason a JSON schema cannot be created
     */
    public void generateAndSaveJSONSchema(final Class<?> clazz) throws JsonProcessingException {
        generateAndSaveJSONSchema(clazz, "./");
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
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonSchema);
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

}
