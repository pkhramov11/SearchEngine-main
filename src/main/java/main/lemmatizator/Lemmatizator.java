package main.lemmatizator;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Lemmatizator {
    public static final String WORD_SEPARATORS =
            "\\s*(\\s|,|;|\\?|-|–|—|\\[|]|\\{|}|«|»|'|'|`|\"|!|\\.|\\(|\\))\\s*";
    private final static LuceneMorphology morphology;

    static {
        try {
            morphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> decomposeTextToLemmas(String text) {
        List<String> result = new ArrayList<>();
        String[] words = text.split(WORD_SEPARATORS);

        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            List<String> lemmas = processOneWord(word);
            for (String lemma : lemmas) {
                result.add(lemma);
            }
        }
        return result;
    }

    public static List<String> processOneWord(String word) {
        List<String> result = new ArrayList<>();
        word = word.toLowerCase(Locale.ROOT)
                .replaceAll("ё", "е");
        try {
            List<String> infos = morphology.getMorphInfo(word);
            for (String info : infos) {
                String lemma = morphInfoToLemma(info);
                if (!lemma.isEmpty())
                    result.add(lemma);
            }
        } catch (Exception e) {
        }
        return result;
    }

    private static String morphInfoToLemma(String morphInfo) {
        int pos = morphInfo.indexOf('|');
        if (pos < 0) {
            return "";
        }
        String wordType = String.valueOf(morphInfo.charAt(pos + 1));
        if (wordType.matches("[nfoklp]")) {
            return "";
        }
        return morphInfo.substring(0, pos);
    }
}
