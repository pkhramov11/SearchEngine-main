package main.model;

import lombok.Data;
import main.builders.SiteService;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "site")
@Data
public class Site implements Serializable {
    public static final String INDEXING = "INDEXING";
    public static final String INDEXED = "INDEXED";
    public static final String FAILED = "FAILED";
    public static final String REMOVING = "REMOVING";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(columnDefinition = "enum('INDEXING','INDEXED','FAILED','REMOVING')",
            nullable = false)
    private String type;
    @Column(name = "status_time", columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime statusTime;
    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;
    @Column(nullable = false)
    private String url;
    @Column(nullable = false)
    private String name;
    @OneToMany(mappedBy = "site",
            fetch = FetchType.LAZY
    )
    private Set<Page> pages = new HashSet<>();

    @OneToMany(mappedBy = "site",
            fetch = FetchType.LAZY
    )
    private Set<Lemma> lemmas = new HashSet<>();
    public String getLastError() {
        return lastError == null ? "" : lastError;
    }

    @Transient
    private SiteService.SiteBuilder siteBuilder;

    @Transient
    private Long lastPageReadingTime = 0L;

    @Override
    public String toString() {
        return "id: " + id + ", url: " + url + ", name: " + name;
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == Site.class;
    }
}
