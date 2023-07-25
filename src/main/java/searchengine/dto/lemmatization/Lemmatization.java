package searchengine.dto.lemmatization;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lemmatization{
/*    public static void main(String[] args) throws IOException {
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        List<String> wordBaseForms = luceneMorph.getNormalForms("леса");
        wordBaseForms.forEach(System.out::println);
    }*/
    public Map<String, Integer> lemmas(String text) throws IOException{
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();

        Map<String, Integer> lemmas = new HashMap<>();
        String[] words = text.split("\\s+");
            for(int i = 0; i < words.length; i++){
                List<String> forms = luceneMorph.getNormalForms(words[i]);
                for(String form : forms){
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
