package main.apiResponses;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import main.model.Site;
import main.repository.Repos;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class StatisticsResponse extends Response {
    private Statistics statistics;

    public StatisticsResponse(boolean emptyIndexing) {
        this.statistics = new Statistics(emptyIndexing);
    }

    @Data
    @Getter
    public static class Statistics {
        private TotalStatistics total;
        private List<DetailedStatistics> detailed;

        public Statistics(boolean emptyIndexing) {
            total = new TotalStatistics(emptyIndexing);
            detailed = new ArrayList<>();

            List<Site> sites = Repos.siteRepo.findAll().stream()
                    .filter(site -> site.getType().equals(Site.INDEXED) ||
                            site.getType().equals(Site.FAILED) ||
                            site.getType().equals(Site.INDEXING))
                    .toList();
            for (Site site : sites) {
                DetailedStatistics detailedStatistics = new DetailedStatistics(site);
                detailed.add(detailedStatistics);
            }
        }
    }

    @Data
    public static class TotalStatistics {
        private int sites;
        private int pages;
        private int lemmas;
        private boolean isIndexing;

        public TotalStatistics(boolean emptyIndexing) {
            int siteCount = Repos.siteRepo.countByType(Site.INDEXED) +
                    Repos.siteRepo.countByType(Site.FAILED);
            setSites(siteCount);

            List<Site> indexedSites = Repos.siteRepo.findAllByType(Site.INDEXED);
            setPages(Repos.pageRepo.countBySites(indexedSites));
            setLemmas(Repos.lemmaRepo.countBySites(indexedSites));

            setIndexing(!emptyIndexing);
        }
    }

    @Data
    static class DetailedStatistics {
        private String url;
        private String name;
        private String status;
        private long statusTime;
        private String error;
        private int pages;
        private int lemmas;

        public DetailedStatistics(Site site) {
            url = site.getUrl();
            name = site.getName();
            status = site.getType();
            statusTime = (site.getStatusTime().toEpochSecond(ZoneOffset.UTC) - 3 * 3600) * 1000;
            error = site.getLastError();
            pages = Repos.pageRepo.countBySite(site);
            lemmas = Repos.lemmaRepo.countBySite(site);
        }
    }
}
