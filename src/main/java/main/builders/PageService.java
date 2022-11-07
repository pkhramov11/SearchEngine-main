package main.builders;

import lombok.extern.slf4j.Slf4j;
import main.config.Props;
import main.model.Index;
import main.model.Lemma;
import main.model.Page;
import main.model.Site;
import main.repository.Repos;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@DependsOn({"siteService", "indexService"})
@Slf4j
public class PageService {
    public static final String OK = "OK";
    public static final String NOT_FOUND = "\"Данная страница находится за пределами сайтов, " +
            "указанных в конфигурационном файле";
    public static final String SITE_NOT_INDEXED = "Нельзя индексировать страницу " +
            "сайта, если сайт ещё не индексирован";
    public static final String RUNNING = "Индексация уже запущена";

    private SiteService siteService;

    private IndexService indexService;

    @Autowired
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    @Autowired
    public void setIndexService(IndexService indexService) {
        this.indexService = indexService;
    }

    public String indexPage(String stringUrl) {
        URL url;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            return NOT_FOUND;
        }
        String home = url.getProtocol() + "://" + url.getHost();
        String path = url.getFile();

        if (siteService.getIndexingSites().containsKey(home)) {
            return RUNNING;
        }

        if (!Props.getAllSiteUrls().contains(home)) {
            return NOT_FOUND;
        }
        Site site = Repos.siteRepo.findByUrlAndType(home, Site.INDEXED).orElse(null);

        if (path.isEmpty()) {
            siteService.buildSingleSite(home);
        } else {
            if (site == null) {
                return SITE_NOT_INDEXED;
            }
            PageBuilder pageBuilder = new PageBuilder(site, path);
            if (pageBuilder.page == null) {
                return NOT_FOUND;
            }
            siteService.getIndexingSites().put(site.getUrl(), site);
            pageBuilder.run();
        }

        return OK;
    }
    public class PageBuilder implements Runnable {
        private final Site site;
        private List<Page> oldPages;
        private Page page = null;

        public PageBuilder(Site site, String pagePath) {
            this.site = site;
            oldPages = Repos.pageRepo.findAllBySiteAndPathAndCode(site, pagePath, Node.OK);

            Node node = new Node(site, pagePath, siteService);
            node.setFromPageBuilder(true);
            Document doc = node.processAndReturnPageDoc();
            if (doc == null) {
                return;
            }
            int id = node.getAddedPageId();
            page = Repos.pageRepo.findById(id).orElse(null);
            if (page == null) {
                doc = null;
                return;
            }
            page.setContent(doc.outerHtml());
            page.setPath(pagePath);
        }

        @Override
        public void run() {
            log.info("Проиндексирована страница " + site.getUrl() + page.getPath());
            List<Lemma> lemmaList = Repos.lemmaRepo.findAllBySite(site);
            Map<String, Lemma> lemmas = new HashMap<>();
            for (Lemma lemma : lemmaList) {
                lemmas.put(lemma.getLemma(), lemma);
            }

            List<Index> indexList = Repos.indexRepo.findAllBySite(site);
            Map<Integer, Index> indices = new HashMap<>();
            int counter = 0;
            for (Index index : indexList) {
                if (counter % 100 == 0) {
                    System.out.println(counter);
                }
                indices.put(index.hashCode(), index);
                counter++;
            }

            indexService.fillLemmasAndIndices(site, page, lemmas, indices);

            List<Lemma> lemmasToDelete = new ArrayList<>();
            if (oldPages != null && oldPages.size() > 0) {
                List<Integer> oldPageIds = oldPages.stream().map(p -> p.getId()).toList();
                for (Index index : indices.values().stream()
                        .filter(index -> oldPageIds.contains(index.getPage().getId()))
                        .toList()) {
                    Lemma lemma = index.getLemma();
                    lemma.setFrequency(lemma.getFrequency() - 1);
                    if (lemma.getFrequency() == 0) {
                        lemmas.remove(lemma.getLemma());
                        lemmasToDelete.add(lemma);
                    }
                }
            }

            synchronized (Lemma.class) {
                Repos.lemmaRepo.deleteAllInBatch(lemmasToDelete);
            }

            List<Index> pageIndices = new ArrayList<>();
            pageIndices.addAll(indices.values().stream()
                    .filter(index -> index.getPage().getId() == page.getId())
                    .toList());

            synchronized (Page.class) {
                Repos.pageRepo.saveAndFlush(page);
            }
            synchronized (Lemma.class) {
                Repos.lemmaRepo.deleteAllInBatch(lemmasToDelete);
                Repos.lemmaRepo.saveAllAndFlush(lemmaList);
            }
            synchronized (Index.class) {
                Repos.indexRepo.saveAllAndFlush(pageIndices);
            }
            synchronized (Page.class) {
                if (oldPages != null) {
                    for (Page p : oldPages) {
                        Repos.pageRepo.deleteById(p.getId());
                    }
                }
            }

            siteService.getIndexingSites().remove(site.getUrl());
        }


    }
}