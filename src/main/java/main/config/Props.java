package main.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "config")
@Data
public class Props {
    private Integer forSitesThreadNumber;
    private Integer forPagesThreadNumber;
    private Integer repeatedPageCount;
    private Integer maxPagesInSite;
    private List<SiteProps> sites;

    private Boolean synchronizePageSave;
    private String multiInsertString;

    private static Boolean inited = false;

    private static Props inst;

    public Props() {
        inst = this;
    }

    public static Props getInst() {
        synchronized (Props.class) {
            if (!inited) {
                init();
                inited = true;
            }
        }
        return inst;
    }

    public static void init() {
        for (SiteProps sun : inst.sites) {
            URL url;
            try {
                url = new URL(sun.getUrl());
            } catch (MalformedURLException e) {
                continue;
            }
            sun.setUrl(url.getProtocol() + "://" + url.getHost());
        }
    }

    public static List<String> getAllSiteUrls() {
        List<String> siteUrls = new ArrayList<>();
        List<SiteProps> urlNames = Props.getInst().getSites();
        urlNames.forEach(sun -> siteUrls.add(sun.getUrl()));
        return siteUrls;
    }


    @Data
    public static class SiteProps {
        private String url;
        private String name;
        private int pause;

        public static String getNameByUrl(String url) {
            SiteProps siteProps = Props.inst.sites.stream()
                    .filter(siteUrlName -> siteUrlName.getUrl().equals(url))
                    .findFirst().orElse(null);
            return siteProps != null ? siteProps.getName() : "";
        }

        public static int getPauseBySiteName(String name) {
            SiteProps siteProps = Props.inst.sites.stream()
                    .filter(siteUrlName -> siteUrlName.getName().equals(name))
                    .findFirst().orElse(null);
            return siteProps != null ? siteProps.getPause() : 0;
        }
    }

}
