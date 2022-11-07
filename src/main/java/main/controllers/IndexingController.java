package main.controllers;

import main.apiResponses.ErrorResponse;
import main.apiResponses.Response;
import main.builders.PageService;
import main.builders.SiteService;
import main.config.ServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexingController {
    public static final String INDEXING_IS_PROHIBITED = "Индексация на этом сервере запрещена";
    public static final String INDEXING_IS_RUNNING = "Индексация уже запущена";
    public static final String INDEXING_NOT_STARTED = "Индексация не была запущена";
    private final ServerConfig serverConfig;

    private SiteService siteService;
    private PageService pageService;

    @Autowired
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    @Autowired
    public void setPageService(PageService pageService) {
        this.pageService = pageService;
    }

    public IndexingController(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @GetMapping("/startIndexing")
    public Response startIndexing() {
        if (!serverConfig.isIndexingAvailable()) {
            return new ErrorResponse(INDEXING_IS_PROHIBITED);
        }
        boolean isIndexing = siteService.buildAllSites();
        if (isIndexing) {
            return new ErrorResponse(INDEXING_IS_RUNNING);
        }
        return new Response();
    }

    @GetMapping("/stopIndexing")
    public Response stopIndexing() {
        boolean isIndexing = siteService.stopIndexing();
        if (isIndexing) {
            return new Response();
        }
        return new ErrorResponse(INDEXING_NOT_STARTED);
    }

    @PostMapping("/indexPage")
    public Response indexPage(@RequestParam(required = false) String url) {
        if (!serverConfig.isIndexingAvailable()) {
            return new ErrorResponse(INDEXING_IS_PROHIBITED);
        }
        String result = pageService.indexPage(url);
        if (result.equals(PageService.OK)) {
            return new Response();
        }
        return new ErrorResponse(result);
    }
}
