package main.controllers;


import main.apiResponses.ErrorResponse;
import main.apiResponses.Response;
import main.builders.SiteService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class IndexingControllerTest extends BaseTest {
    private static final long INDEXING_STOP_TIMEOUT = 10000;

    @Autowired
    private IndexingController controller;

    @Autowired
    private SiteService siteService;

    @Test
    public void startIndexingTest() throws Exception {
        try {
            Response response = controller.startIndexing();
            Assertions.assertTrue(response.isResult());
            Thread.sleep(INDEXING_STOP_TIMEOUT);
            response = controller.startIndexing();
            Assertions.assertFalse(response.isResult());
        }
        finally {
            controller.stopIndexing();
            Thread.sleep(INDEXING_STOP_TIMEOUT);
        }
    }

    @Test
    public void stopIndexingTest() throws Exception {
        Response response = controller.stopIndexing();
        Assertions.assertFalse(response.isResult());
        controller.startIndexing();
        response = controller.stopIndexing();
        Assertions.assertTrue(response.isResult());
        Thread.sleep(INDEXING_STOP_TIMEOUT);
    }


//    @Test
//    public void indexPageTest() throws Exception {
//        try {
//            Response response = controller.indexPage(TEST_PAGE);
//            Assertions.assertTrue(response.isResult());
//        } finally {
//            controller.stopIndexing();
//            Thread.sleep(INDEXING_STOP_TIMEOUT);
//        }
//    }
}
