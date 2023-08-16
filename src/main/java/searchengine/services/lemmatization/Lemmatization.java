package searchengine.services.lemmatization;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.querydsl.QPageRequest;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.services.repositories.IndexRepository;
import searchengine.services.repositories.LemmaRepository;
import searchengine.services.repositories.PageRepository;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lemmatization{

    private PageRepository pageRepository;

    private LemmaRepository lemmaRepository;
    private IndexRepository indexRepository;
    private int siteId;

    private Lemmatization(LemmaRepository lemmaRepository, IndexRepository indexRepository){
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
    }
    private Lemmatization(PageRepository pageRepository, int siteId){
        this.pageRepository = pageRepository;
        this.siteId = siteId;
    }

    public void lemmatizationIndexing(String url){
        try {
            Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36").get();
            Element html = doc.select("html").first();
            String htmlText = html.outerHtml();
            Page page = new Page();
            page.setContent(htmlText);
            page.setPath(url);
            Connection.Response response;
            response = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36").execute();
            int code = response.statusCode();
            page.setCode(code);
            pageRepository.save(page);

            Map<String, Integer> lemmas = lemmas(htmlText);
            Set<String> keys = lemmas.keySet();
            for(String key : keys){
                Lemma oldLemma = lemmaRepository.findLemmaByLemma(key);
                if(oldLemma != null){
                    int oldFrequency = oldLemma.getFrequency();
                    int newFrequency = oldFrequency++;
                    oldLemma.setFrequency(newFrequency);
                    lemmaRepository.save(oldLemma);
                    continue;
                }
                Lemma lemma = new Lemma();
                lemma.setLemma(key);
                lemma.setSiteId(siteId);
                lemma.setFrequency(1);
                lemmaRepository.save(lemma);
            }

        } catch(IOException exception){
            exception.printStackTrace();
        }
          }
    public Map<String, Integer> lemmas(String text) throws IOException{
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        Map<String, Integer> lemmas = new HashMap<>();
        String cleared = clearHtmlTags(text);
        String punctuationReplaced = cleared.replaceAll("\\p{P}", "");
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

    private String clearHtmlTags(String unclearedText){
        String regex = "</?.+?>";
        String text = unclearedText.replaceAll(regex, "");
        return text;
    }
}
