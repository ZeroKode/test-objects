package com.github.zerokode.testobjects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.zerokode.testobjects.example.Order;
import com.github.zerokode.testobjects.example.Product;
import com.github.zerokode.testobjects.example.Status;
import com.github.zerokode.testobjects.example.User;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

public class TestObjectsTest {

    private static final Logger log = Logger.getLogger(TestObjectsTest.class);

    private TestObjectsReader testObjectsReader;
    private TestObjectsSchemaGenerator testObjectsSchemaGenerator;

    @Before
    public void setup() {
        log.info("Setting up an Object Mapper used for testing. End users must provide a custom Object Mapper instance.");
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        testObjectsReader = new TestObjectsReader(objectMapper);
        testObjectsSchemaGenerator = new TestObjectsSchemaGenerator(objectMapper);
    }

    @Test
    public void testCreateAllJsonSchemas() {
        try {
            log.info("Generating JSON Schema files");
            testObjectsSchemaGenerator.generateAndSaveJSONSchema(Product.class);
            testObjectsSchemaGenerator.generateAndSaveJSONSchema(Order.class);
            testObjectsSchemaGenerator.generateAndSaveJSONSchema(User.class);
        } catch (JsonProcessingException e) {
            Assert.fail("Failed to generate JSON Schemas");
        }
    }

    @Test
    public void testLoadProduct() {
        Product laptop = testObjectsReader.read("objects/product-laptop.json", Product.class);
        Assert.assertEquals("Bell Laptop", laptop.getName());
        Assert.assertEquals("P123", laptop.getId());
        Assert.assertEquals(1450.99D, laptop.getPrice(), 0.0001);
        Assert.assertEquals(1109.99, laptop.getCost(), 0.0001);
    }

    @Test
    public void testLoadOrder() {
        Order order = testObjectsReader.read("objects/order-001.json", Order.class);
        Assert.assertEquals("0001", order.getId());
        Assert.assertEquals(2, order.getProducts().size());
        Assert.assertEquals(399.99d, order.getTotalPaid(), 0.0001);
        Assert.assertEquals(1123123123123L, order.getDate().getEpochSecond());
    }

    @Test
    public void tesLoadUserYaml() {
        ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());
        yamlObjectMapper.findAndRegisterModules();
        TestObjectsReader testObjectsReaderYaml = new TestObjectsReader(yamlObjectMapper);

        User user = testObjectsReaderYaml.read("objects/user-john.yml", User.class);
        Assert.assertNotNull(user);
        Assert.assertEquals("John Doe", user.getName());
        Assert.assertEquals(2018, user.getMemberSince());
        Assert.assertEquals(Status.ACTIVE, user.getStatus());
        Assert.assertEquals(1, user.getOrders().size());

        Optional<Order> firstOrder = user.getOrders().stream().findFirst();
        Assert.assertTrue(firstOrder.isPresent());
        Assert.assertEquals(1999.99, firstOrder.get().getTotalPaid(), 0.0001);

        Optional<Product> firstProductOfFirstOrder = firstOrder.get().getProducts().stream().findFirst();
        Assert.assertTrue(firstProductOfFirstOrder.isPresent());

        Assert.assertEquals("Maple Computer", firstProductOfFirstOrder.get().getName());
        Assert.assertEquals(1999.99, firstProductOfFirstOrder.get().getPrice(), 0.0001);
    }

}
