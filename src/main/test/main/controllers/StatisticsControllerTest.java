package main.controllers;

import main.apiResponses.StatisticsResponse;
import main.builders.SiteService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class StatisticsControllerTest extends BaseTest {
    @Autowired
    private StatisticsController controller;

    @Autowired
    private SiteService siteService;

    @Test
    public void statisticsTest() throws Exception {
        siteService.buildSite(TEST_SITE);
        Thread.sleep(TEST_RUN_TIMEOUT);
        StatisticsResponse statisticsResponse = (StatisticsResponse) controller.statistics();
        Assertions.assertEquals(1, statisticsResponse.getStatistics().getTotal().getSites());
        Assertions.assertEquals(99, statisticsResponse.getStatistics().getTotal().getPages());
        Assertions.assertEquals(3293, statisticsResponse.getStatistics().getTotal().getLemmas());
    }
}
