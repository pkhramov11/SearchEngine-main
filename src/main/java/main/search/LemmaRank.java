package main.search;

import lombok.Data;

@Data
public class LemmaRank {
    private String lemma;
    private float rank;

    public LemmaRank(String lemma, float rank) {
        this.lemma = lemma;
        this.rank = rank;
    }
}
