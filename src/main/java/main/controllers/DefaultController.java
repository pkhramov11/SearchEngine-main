package main.controllers;

import main.config.ServerConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DefaultController {
    private final ServerConfig serverConfig;

    public DefaultController(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @RequestMapping("/")
    public String defaultPage(Model model) {
        return "index";
    }

}
