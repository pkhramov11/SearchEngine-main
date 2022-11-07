package main.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DefaultControllerTest {
    @Autowired
    private DefaultController controller;

    @Test
    public void defaultPageTest() {
        Assertions.assertEquals("index", controller.defaultPage(null ));
    }
}
