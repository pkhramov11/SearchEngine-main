package main.repository;

import main.model.Page;
import main.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    @Query(value = "select count(*) from Page p where p.site in :sites")
    Integer countBySites(@Param("sites") Collection<Site> siteList);
    Integer countBySite(Site site);
    List<Page> findAllBySiteAndPathAndCode(Site site, String path, int code);
}
