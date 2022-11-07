package main.model;

import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Table(name = "lemma",
//        uniqueConstraints={@UniqueConstraint(columnNames={"lemma", "site_id"},
//                name = "UK_lemma_lemma_site")},
        indexes = {@javax.persistence.Index(columnList = "lemma",
                name = "KEY_lemma_lemma")})
@Data
public class Lemma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(columnDefinition = "varchar(255)", nullable = false)
    private String lemma;
    @Column(nullable = false)
    private float frequency;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_lemma_site"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Site site;

    @Transient
    private float weight;

    @Override
    public boolean equals(Object obj) {
        Lemma l = (Lemma) obj;
        return lemma.equals(l.lemma) && site == l.site;
    }

    @Override
    public int hashCode() {
        return lemma.hashCode() + site.hashCode();
    }

    @Override
    public String toString() {
        return "id: " + id + "; lemma: " + lemma + "; frequency: " + frequency + "; site: " + site.getName();
    }
}
