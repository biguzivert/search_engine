package searchengine.config;

import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.Objects;

@Getter
@Setter
public class Lemma implements Comparable<Lemma> {

    private String lemma;
    private Integer frequency;

    public Lemma(String lemma, Integer frequency){
        this.lemma = lemma;
        this.frequency = frequency;
    }

    @Override
    public int compareTo(Lemma lemma) {
        return this.getFrequency() - lemma.getFrequency();
    }
}
