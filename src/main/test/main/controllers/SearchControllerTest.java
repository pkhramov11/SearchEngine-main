package main.controllers;

import main.apiResponses.SearchResponse;
import main.builders.SiteService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SearchControllerTest extends BaseTest {
    @Autowired
    private SearchController controller;

    @Autowired
    private SiteService siteService;

    @Test
    public void searchTest() throws Exception {
        siteService.buildSite(TEST_SITE);
        Thread.sleep(TEST_RUN_TIMEOUT);
        SearchResponse searchResponse = (SearchResponse) controller.search("мельбурн", null, 0, 1000);
        Assertions.assertEquals(0, searchResponse.getCount());

        searchResponse = (SearchResponse) controller.search("умывальник", null, 0, 1000);
        Assertions.assertEquals(1, searchResponse.getCount());
    }
}