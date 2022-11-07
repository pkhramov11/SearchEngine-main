package main.builders;

import lombok.extern.slf4j.Slf4j;
import main.lemmatizator.Lemmatizator;
import main.model.*;
import main.repository.Repos;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class IndexService {
    public final String TABS = "\t\t";

    private SiteService siteService;

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void build(Site site) {
        synchronized (Lemma.class) {
            Repos.lemmaRepo.deleteAllInBatchBySite(site);
        }
        log.info(TABS + "Сайт \"" + site.getName() + "\": строим леммы и индексы");
        IndexBuilder indexBuilder = new IndexBuilder(site, null, null, null);
        indexBuilder.buildIndex();
        indexBuilder.saveLemmasAndIndices();
    }

    public void fillLemmasAndIndices(Site site, Page page, Map<String, Lemma> lemmas, Map<Integer, Index> indices) {
        IndexBuilder indexBuilder = new IndexBuilder(site, page, lemmas, indices);
        indexBuilder.fillLemmasAndIndices();
    }

    private class IndexBuilder {
        private final Site site;
        private final Page page;
        private Map<String, Lemma> lemmas;
        private Map<Integer, Index> indices;
        private final Set<String> lemmasInPage;

        private static List<Field> fields;

        public static List<Field> getFields() {
            synchronized (Field.class) {
                if (fields == null) {
                    fields = Repos.fieldRepo.findAll();
                }
                return fields;
            }
        }

        public IndexBuilder(Site site, Page page, Map<String, Lemma> lemmas, Map<Integer, Index> indices) {
            this.site = site;
            this.page = page;
            this.lemmas = lemmas;
            this.indices = indices;
            lemmasInPage = new HashSet<>();
        }



        private void buildIndex() {
            lemmas = new HashMap<>();
            indices = new HashMap<>();

            List<Page> pages = site.getPages().stream()
                    .filter(p1 -> p1.getCode() == Node.OK)
                    .sorted(Comparator.comparingInt(Page::getId)).toList();
            for (Page page : pages) {
                if (siteService.isStopping()) {
                    return;
                }
                Page pag;
                pag = Repos.pageRepo.findById(page.getId()).orElse(null);
                if (pag == null) {
                    continue;
                }
                IndexBuilder indexBuilder = new IndexBuilder(
                        site, pag, lemmas, indices);
                indexBuilder.fillLemmasAndIndices();
                pag.setContent(null);
                pag.setPath(null);
            }
        }

        public void fillLemmasAndIndices() {
            Document doc = Jsoup.parse(page.getContent());
            for (Field field : getFields()) {
                Elements elements = doc.getElementsByTag(field.getSelector());
                for (Element element : elements) {
                    String text = element.text();
                    List<String> lemmaNames = Lemmatizator.decomposeTextToLemmas(text);
                    for (String lemmaName : lemmaNames) {
                        insertIntoLemmasAndIndices(lemmaName, field.getWeight());
                    }
                }
            }
        }

        private void insertIntoLemmasAndIndices(String lemmaName, float weight) {
            Lemma lemma = lemmas.get(lemmaName);
            if (lemma == null) {
                lemma = new Lemma();
                lemma.setLemma(lemmaName);
                lemma.setFrequency(1);
                lemma.setSite(site);
                lemma.setWeight(weight);
                lemmas.put(lemmaName, lemma);

                Index index = new Index(page, lemma, weight);
                indices.put(index.hashCode(), index);

                lemmasInPage.add(lemmaName);
                return;
            }
            if (lemmasInPage.contains(lemmaName)) {
                Index auxIndex = new Index(page, lemma, 0);
                Index index = indices.get(auxIndex.hashCode());
                index.setRank(index.getRank() + weight);
            } else {
                lemmasInPage.add(lemmaName);
                lemma.setFrequency(lemma.getFrequency() + 1);
                Index index = new Index(page, lemma, weight);
                indices.put(index.hashCode(), index);
            }
        }
        public void saveLemmasAndIndices() {
            log.info(TABS + "Сайт \"" + site.getName() + "\": cохраняем леммы");
            if (siteService.isStopping()) {
                return;
            }
            var lemmaCollection = lemmas.values();
            synchronized (Lemma.class) {
                Repos.lemmaRepo.saveAllAndFlush(lemmaCollection);
            }

            log.info(TABS + "Сайт \"" + site.getName() + "\": cохраняем индексы");
            saveIndicesByMultipleInsert();

            log.info(TABS + "Сайт \"" + site.getName() + "\": " +
                    "всего сохранено страниц - " + site.getPages().size());
        }

        private void saveIndicesBySaveAllAndFlush() {
            int ind = 1;
            for (Page page : site.getPages()) {
                if (siteService.isStopping()) {
                    return;
                }
                List<Index> pageIndices = indices.values().stream()
                        .filter(index -> index.getPage().getId() == page.getId()
                                && index.getPage().getCode() == Node.OK)
                        .toList();
                synchronized (Index.class) {
                    Repos.indexRepo.saveAllAndFlush(pageIndices);
                }
                ind++;
                if (ind % 100 == 0) {
                    log.info(TABS + "Сайт \"" + site.getName() + "\": сохранено страниц - " + ind);
                }
            }
        }

        private void saveIndicesByMultipleInsert() {
            List<Index> siteIndices = indices.values().stream()
                    .filter(index -> index.getPage().getSite().getId() == site.getId()
                            && index.getPage().getCode() == Node.OK)
                    .toList();
            if (siteIndices.size() == 0) {
                return;
            }
            String siteName = site.getName();
            Repos.indexImplRepo.insertIndexList(siteName, siteIndices);
        }
    }
}
