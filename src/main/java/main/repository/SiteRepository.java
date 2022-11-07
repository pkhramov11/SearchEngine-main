package main.repository;

import main.model.Site;
import main.model.Index;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {
    Optional<Site> findByUrlAndType(String url, String type);
    Optional<Site> findByNameAndType(String name, String type);
    Integer countByType(String type);
    List<Site> findAllByType(String type);
    @Transactional
    void deleteAllByType(String type);
}
