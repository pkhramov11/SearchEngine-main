package main.builders;

import lombok.extern.slf4j.Slf4j;
import main.config.Props;
import main.model.Page;
import main.model.Site;

import lombok.Data;
import main.repository.Repos;
import main.utils.RandomUserAgent;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;

@Data
@Slf4j
public class Node {
    public static final int OK = 200;
    public static final int JS_UNKNOWN = -600;
    public static final int JS_HTTP_ERROR_FETCHING_URL_500 = 600;
    public static final int JS_UNAUTHORIZED = 601;
    public static final int JS_HTTP_ERROR_FETCHING_URL_502 = 602;
    public static final int JS_FORBIDDEN = 603;
    public static final int JS_NOT_FOUND = 604;
    public static final int JS_UNSUPPORTED_MEDIA_TYPE = 605;
    public static final int JS_UNEXPECTED_END_OF_ZLIB = 606;
    public static final int JS_CONNECTION_REFUSED = 607;
    public static final int JS_CONNECTION_TIMED_OUT = 608;
    public static final int JS_READ_TIMED_OUT = 609;

    private static Set<String> EXTEMSIONS_TO_SKIP = new HashSet<>(Arrays.asList(
            ".jpg", ".pdf", ".png", "jpeg", "jfif", ".doc", ".docx", ".xls",
            ".xlsx", ".pptx", ".rtf", ".mp4"));

    private Site site;
    private String pagePath;
    private Set<String> viewedNodes;
    private ConcurrentLinkedQueue<String> lastNodes;
    private CopyOnWriteArraySet<String> forbiddenNodes;
    private int addedPageId;
    private boolean fromPageBuilder = false;

    private SiteService siteService;

    public Node(Site site, String pagePath, SiteService siteService) {
        this.site = site;
        this.pagePath = pagePath;
        if (site.getSiteBuilder() != null) {
            viewedNodes = site.getSiteBuilder().getViewedPages();
            lastNodes = site.getSiteBuilder().getLastNodes();
            forbiddenNodes = site.getSiteBuilder().getForbiddenNodes();
            if (viewedNodes.isEmpty()) {
                viewedNodes.add("/");
            }
        }
        this.siteService = siteService;
    }

    private String getUrl() {
        return site.getUrl() + pagePath;
    }

    public Document processAndReturnPageDoc() {
        if (siteService.isStopping()) {
            return null;
        }

        Page page = new Page();

        page.setSite(site);
        page.setPath(pagePath);
        page.setCode(OK);
        page.setContent("");

        Document doc = null;
        try {
            Connection.Response response = Jsoup.connect(getUrl())
                    .userAgent(RandomUserAgent.getRandomUserAgent())
                    .referrer("http://www.google.com")
                    .execute();
            int statusCode = response.statusCode();
            if (statusCode != OK) {
                page.setCode(statusCode);
                return null;
            }
            doc = Jsoup.parse(response.body());
            page.setContent(doc.outerHtml());
        } catch (Exception e) {
            String message = e.toString();

            int errorCode;
            if (message.contains("UnsupportedMimeTypeException")) {
                errorCode = 415;    // Ссылка на pdf, jpg, png документы
            } else if (message.contains("Status=401")) {
                errorCode = 401;    // На несуществующий домен
            } else if (message.contains("UnknownHostException")) {
                errorCode = 401;
            } else if (message.contains("Status=403")) {
                errorCode = 403;    // Нет доступа, 403 Forbidden
            } else if (message.contains("Status=404")) {
                if (fromPageBuilder) {
                    return null;
                }
                errorCode = 404;    // // Ссылка на pdf-документ, несущ. страница, проигрыватель
            } else if (message.contains("Status=500")) {
                errorCode = 401;    // Страница авторизации
            } else if (message.contains("ConnectException: Connection refused")) {
                errorCode = 500;    // ERR_CONNECTION_REFUSED, не удаётся открыть страницу
            } else if (message.contains("SSLHandshakeException")) {
                errorCode = 525;
            } else {
                errorCode = -1;
            }
            page.setCode(errorCode);
        }

        if (page.getCode() != OK && page.getPath().equals("/")) {
            switch (page.getCode()) {
                case 401 -> site.setLastError("Обращение к несуществующему домену");
                case 403 -> site.setLastError("Главная страница недоступна");
                case 525 -> site.setLastError("Kвитирование SSL не удалось");
            }
            return doc;
        }

        if (Props.getInst().getSynchronizePageSave()) {
            synchronized (Page.class) {
                Repos.pageRepo.saveAndFlush(page);
            }
        } else {
            Repos.pageRepo.saveAndFlush(page);
        }

        synchronized (Site.class) {
            site.getPages().add(page);
        }
        addedPageId = page.getId();

        page.setContent(null);

        if(!fromPageBuilder) {
            log.info(site.getUrl() + pagePath);
        }

        return doc;
    }

    public List<Node> getChildren(Document doc) {
        List<Node> children = new ArrayList<>();

        if (doc == null) {
            return null;
        }
        Element body = doc.body();
        if (body == null) {
            return null;
        }

        viewTagsA(body, children);
        viewAttributesOnclick(body, children);
        while (lastNodes.size() > Props.getInst().getRepeatedPageCount()) {
            lastNodes.poll();
        }

        return children;
    }

    private void viewTagsA(Element body, List<Node> children) {
        Elements links = body.getElementsByTag("a");
        linkCycle:
        for (Element link : links) {
            String href = link.attr("href");
            if (href.startsWith("http")) {
                if (!href.startsWith(site.getUrl())) {
                    continue;
                }
                href = href.substring(site.getUrl().length());
            }
            if (!href.startsWith("/") || href.startsWith("#")) {
                continue;
            }
            if (href.contains("/?")) {
                continue;
            }
            if (href.contains("?PAGE")) {
                continue;
            }
            if (href.startsWith("//")) {
                continue;
            }
            if (href.endsWith("//")) {
                href = href.substring(0, href.length() - 1);
            }
            String hrefLow = href.toLowerCase(Locale.ROOT);
            for (String ext : EXTEMSIONS_TO_SKIP) {
                if (hrefLow.contains(ext)) {
                    continue linkCycle;
                }
            }
            permissabilitySupport(href, children);
        }
    }

    private void viewAttributesOnclick(Element body, List<Node> children) {
        Elements elements = body.getElementsByAttribute("onclick");
        for (Element elem : elements) {
            String attrContent = elem.attr("onclick").intern();
            if (attrContent.startsWith("window.location") ||
                    attrContent.startsWith("location.href")) {
                int begin = attrContent.indexOf("'") + 1;
                int end = attrContent.indexOf("'", begin);
                attrContent = attrContent.substring(begin, end);

                String contLow = attrContent.toLowerCase(Locale.ROOT);
                for (String ext : EXTEMSIONS_TO_SKIP) {
                    if (contLow.contains(ext)) {
                        continue;
                    }
                }

                permissabilitySupport(attrContent, children);
            }
        }
    }

    private static Integer viewedPagesMonitor = 0;

    private void permissabilitySupport(String content, List<Node> children) {
        boolean added;
        synchronized (Node.class) {
            added = viewedNodes.add(content);
        }
        if (isForbidden(content)) {
            return;
        }
        if (added) {
            Node node = new Node(site, content, siteService);
            children.add(node);
            lastNodes.add(content);
        }
    }

    private boolean isForbidden(String node) {
        int pos = node.indexOf("/", 1);
        if (pos > 0) {
            pos = node.indexOf("/", pos + 1);
            if (pos > 0) {
                node = node.substring(0, pos);
            }
        }
        if (lastNodes.size() < Props.getInst().getRepeatedPageCount())
            return false;
        for (String s : lastNodes) {
            if (!s.startsWith(node)) {
                return false;
            }
        }
        forbiddenNodes.add(node);
        return true;
    }
}
