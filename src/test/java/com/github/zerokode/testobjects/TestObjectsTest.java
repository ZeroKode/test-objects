package com.github.zerokode.testobjects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.zerokode.testobjects.example.Order;
import com.github.zerokode.testobjects.example.Product;
import com.github.zerokode.testobjects.example.User;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestObjectsTest {

    private static final Logger log = Logger.getLogger(TestObjectsTest.class);

    private TestObjects testObjects;

    @Before
    public void setup() {
        log.info("Setting up an Object Mapper used for testing. End users must provide a custom Object Mapper instance.");
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        testObjects = new TestObjects(objectMapper);
    }

    @Test
    public void testCreateAllJsonSchemas() {
        try {
            log.info("Generating JSON Schema files");
            testObjects.generateAndSaveJSONSchema(Product.class);
            testObjects.generateAndSaveJSONSchema(Order.class);
            testObjects.generateAndSaveJSONSchema(User.class);
        } catch (JsonProcessingException e) {
            Assert.fail("Failed to generate JSON Schemas");
        }
    }

    @Test
    public void testLoadProduct() {
        Product laptop = testObjects.loadTestObject("objects/product-laptop.json", Product.class);
        Assert.assertEquals("Bell Laptop", laptop.getName());
        Assert.assertEquals("P123", laptop.getId());
        Assert.assertEquals(1450.99D, laptop.getPrice(), 0.0001);
        Assert.assertEquals(1109.99, laptop.getCost(), 0.0001);
    }

    @Test
    public void testLoadOrder() {
        Order order = testObjects.loadTestObject("objects/order-001.json", Order.class);
        Assert.assertEquals("0001", order.getId());
        Assert.assertEquals(2, order.getProducts().size());
        Assert.assertEquals(399.99d, order.getTotalPaid(), 0.0001);
        Assert.assertEquals(1123123123123L, order.getDate().getEpochSecond());
    }

}
