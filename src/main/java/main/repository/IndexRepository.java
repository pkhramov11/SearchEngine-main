package main.repository;

import main.model.Index;
import main.model.Lemma;
import main.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<Index, Integer>, IndexRepositoryCustom {
    @Query(value = "select i from Index i join Page p " +
            "on p.site = :site and i.page = p")
    List<Index> findAllBySite(@Param("site") Site site);
    @Query("select i from Index i, Lemma l, Page p, Site s " +
            "where s = :site and i.page = p and p.site = :site " +
            "and i.lemma = l and l.lemma = :textLemma")
    List<Index> findAllByTextLemmaAndSite(
            @Param("textLemma") String lemma, @Param("site") Site site);
    @Query(value = "select i from Index i, Lemma l, Page p, Site s " +
            "where s.type = 'INDEXED' and l.lemma = :textLemma " +
            "and l.site = s and p.site = s " +
            "and i.lemma = l and i.page = p")
    List<Index> findAllByTextLemma(@Param("textLemma") String lemma);
}
