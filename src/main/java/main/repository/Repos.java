package main.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Repos {
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private FieldRepository fieldRepository;
    @Autowired
    private IndexRepository indexRepository;
    @Autowired
    private IndexRepositoryImpl indexRepositoryImpl;

    public static SiteRepository siteRepo;
    public static PageRepository pageRepo;
    public static LemmaRepository lemmaRepo;
    public static FieldRepository fieldRepo;
    public static IndexRepository indexRepo;
    public static IndexRepositoryImpl indexImplRepo;

    public void init() {
        siteRepo = siteRepository;
        pageRepo = pageRepository;
        lemmaRepo = lemmaRepository;
        fieldRepo = fieldRepository;
        indexRepo = indexRepository;
        indexImplRepo = indexRepositoryImpl;
    }
}
