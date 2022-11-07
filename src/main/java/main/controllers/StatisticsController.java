package main.controllers;

import main.apiResponses.Response;
import main.builders.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatisticsController {
    private SiteService siteService;

    @Autowired
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping("/statistics")
    public Response statistics() {
        return siteService.getStatistics();
    }
}
