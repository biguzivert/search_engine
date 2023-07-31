package searchengine.dto.lemmatization;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lemmatization{
    public static void main(String[] args) throws IOException {
    String text = "Повторное появление леопарда в Осетии позволяет предположить, что леопард постоянно обитает в некоторых районах Северного Кавказа.";
    Map<String, Integer> lemma = lemmas(text.toLowerCase());
        System.out.println(lemma);
    }
    public static Map<String, Integer> lemmas(String text) throws IOException{
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        Map<String, Integer> lemmas = new HashMap<>();

        String punctuationReplaced = text.replaceAll("\\p{P}", "");
        String[] words = punctuationReplaced.split("\\s+");
        for (String word : words) {
            List<String> formInfo = luceneMorph.getMorphInfo(word.toLowerCase());
            boolean notAWord = false;
            for (String f : formInfo) {
                if (f.contains("ПРЕД") || f.contains("СОЮЗ")) {
                    notAWord = true;
                    break;
                }
            }
            if (notAWord) {
                continue;
            }
            List<String> forms = luceneMorph.getNormalForms(word.toLowerCase());
            for (String form : forms) {
                if (lemmas.containsKey(form)) {
                    lemmas.put(form, lemmas.get(form) + 1);
                } else {
                    lemmas.put(form, 1);
                }
            }
        }
        return lemmas;
    }
}
