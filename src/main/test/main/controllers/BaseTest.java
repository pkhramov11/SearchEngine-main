package main.controllers;

import main.repository.Repos;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BaseTest {
    protected static final String TEST_SITE = "https://nikoartgallery.com";
    protected static final long TEST_RUN_TIMEOUT = 360000;

    @BeforeEach
    public void init() {
        System.out.println("!!!");
        Repos.siteRepo.deleteAll();
        Repos.pageRepo.deleteAll();
        Repos.lemmaRepo.deleteAll();
        Repos.indexRepo.deleteAll();
    }
}
