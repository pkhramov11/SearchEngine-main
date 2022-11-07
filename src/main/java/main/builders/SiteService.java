package main.builders;

import lombok.extern.slf4j.Slf4j;
import main.apiResponses.StatisticsResponse;
import main.config.Props;
import main.model.Site;
import main.repository.Repos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Service
@DependsOn("props")
@Slf4j
public class SiteService {
    private final boolean IS_INDEXING = true;

    private final boolean SINGLE_SITE_IS_INDEXING = true;
    private ExecutorService executor;
    private final int forSitesThreadNumber = Props.getInst().getForSitesThreadNumber();
    private final ConcurrentHashMap<String, Site>  indexingSites = new ConcurrentHashMap<>();
    private boolean stopping = false;

    private IndexService indexService;

    private PagesOfSiteService pagesOfSiteService;

    @Autowired
    public void setIndexService(IndexService indexService) {
        this.indexService = indexService;
        indexService.setSiteService(this);
    }

    @Autowired
    public void setPagesOfSiteService(PagesOfSiteService pagesOfSiteService) {
        this.pagesOfSiteService = pagesOfSiteService;
        pagesOfSiteService.setSiteService(this);
    }

    public boolean isStopping() {
        return stopping;
    }

    public void setStopping(boolean stopping) {
        this.stopping = stopping;
    }

    public ConcurrentHashMap<String, Site> getIndexingSites() {
        return indexingSites;
    }

    public void buildSite(String siteUrl) {
        synchronized (Executors.class) {
            if (executor == null) {
                executor = Executors.newFixedThreadPool(forSitesThreadNumber);
            }
        }

        SiteBuilder siteBuilder = new SiteBuilder(siteUrl);

        Site processingSite = indexingSites.putIfAbsent(siteUrl, siteBuilder.site);
        if (processingSite != null) {
            return;
        }

        executor.execute(siteBuilder);
    }

    public boolean buildAllSites() {
        if (!indexingSites.isEmpty()) {
            return IS_INDEXING;
        }

        List<Props.SiteProps> sitePropsList = Props.getInst().getSites();
        for (var siteProps : sitePropsList) {
            buildSite(siteProps.getUrl());
        }
        return !IS_INDEXING;
    }

    public void buildSingleSite(String url) {
        String siteName = Props.SiteProps.getNameByUrl(url);
        if (siteName.equals("")) {
            return;
        }

        buildSite(url);
    }

    public boolean stopIndexing() {
        if (indexingSites.isEmpty()) {
            return !IS_INDEXING;
        }

        setStopping(true);

        return IS_INDEXING;
    }

    public  StatisticsResponse getStatistics() {
        return new StatisticsResponse(indexingSites.isEmpty());
    }

    public class SiteBuilder implements Runnable {
        private Site site;
        private final Set<String> viewedPages;
        private final ConcurrentLinkedQueue<String> lastNodes;
        private final CopyOnWriteArraySet<String> forbiddenNodes;

        public ConcurrentLinkedQueue<String> getLastNodes() {
            return lastNodes;
        }

        public CopyOnWriteArraySet<String> getForbiddenNodes() {
            return forbiddenNodes;
        }

        public Set<String> getViewedPages() {
            return viewedPages;
        }
        public SiteBuilder(String siteUrl) {
            lastNodes = new ConcurrentLinkedQueue<>();
            forbiddenNodes = new CopyOnWriteArraySet<>();
            viewedPages = new HashSet<>();

            Optional<Site> indexingSite =
                    Repos.siteRepo.findByUrlAndType(siteUrl, Site.INDEXING);
            if (!indexingSite.isEmpty()) {
                indexingSite.get().setType(Site.REMOVING);
                synchronized (Site.class) {
                    Repos.siteRepo.saveAndFlush(indexingSite.get());
                }
            }

            site = new Site();
            site.setName(Props.SiteProps.getNameByUrl(siteUrl));
            site.setUrl(siteUrl);
            site.setStatusTime(LocalDateTime.now());
            site.setSiteBuilder(SiteBuilder.this);
            site.setType(Site.INDEXING);

            synchronized (Site.class) {
                Repos.siteRepo.saveAndFlush(site);
            }
        }

        @Override
        public void run() {
            log.info("Сайт \"" + site.getName() + "\" индексируется");
            buildPagesLemmasAndIndices();

            if (isStopping()) {
                Site indexingSite = Repos.siteRepo.findByNameAndType(site.getName(), Site.INDEXING)
                        .orElse(null);
                if (indexingSite != null) {
                    indexingSite.setType(Site.REMOVING);
                    synchronized (Site.class) {
                        Repos.siteRepo.saveAndFlush(indexingSite);
                    }
                }
                log.info("Индексация сайта \"" + site.getName() + "\" прервана");
            }

            indexingSites.remove(site.getUrl());
            if (indexingSites.isEmpty()) {
                stopping = false;
            }
        }

        private void buildPagesLemmasAndIndices() {
            long begin = System.currentTimeMillis();
            Site prevSite;
            pagesOfSiteService.build(site);
            if (isStopping()) {
                return;
            }

            indexService.build(site);
            if (isStopping()) {
                return;
            }

            log.info(indexService.TABS + "Сайт \"" + site.getName() + "\" построен за " +
                    (System.currentTimeMillis() - begin) / 1000 + " сек");

            setCurrentSiteAsWorking();
        }

        private void setCurrentSiteAsWorking() {
            Site prevSite = Repos.siteRepo.findByNameAndType(site.getName(), Site.INDEXED)
                    .orElse(null);
            if (prevSite == null) {
                prevSite = Repos.siteRepo.findByNameAndType(site.getName(), Site.FAILED)
                        .orElse(null);
            }
            if (prevSite != null) {
                prevSite.setType(Site.REMOVING);
                synchronized (Site.class) {
                    Repos.siteRepo.saveAndFlush(prevSite);
                }
            }

            if (site.getLastError().isEmpty()) {
                site.setType(Site.INDEXED);
            } else {
                site.setType(Site.FAILED);
            }
            synchronized (Site.class) {
                Repos.siteRepo.saveAndFlush(site);
            }

            synchronized (Site.class) {
                Repos.siteRepo.deleteAllByType(Site.REMOVING);
            }
        }
    }
}