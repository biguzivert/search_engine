package searchengine.services.statistics.lemmatization;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.repositories.IndexRepository;
import searchengine.model.repositories.LemmaRepository;
import searchengine.model.repositories.PageRepository;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

public class Lemmatization{

    private PageRepository pageRepository;
    private LemmaRepository lemmaRepository;
    private IndexRepository indexRepository;
    private int siteId;
    private Site site;

    //@Autowired
    public Lemmatization(Site site, LemmaRepository lemmaRepository, IndexRepository indexRepository, PageRepository pageRepository){
        this.site = site;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.pageRepository = pageRepository;
    }
/*    public Lemmatization(int siteId){
        this.siteId = siteId;
    }*/

    public Lemmatization(){};

    //отследить ошибку если сайт есть в списке конфигураций но его еще нет в site потому что не было индексации
    public void lemmatizationIndexing(){
        try {
            Connection.Response response;
            response = Jsoup.connect(site.getUrl()).userAgent("Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36").execute();
            int responceCode = response.statusCode();
            Document doc = Jsoup.connect(site.getUrl()).userAgent("Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36").get();
            Element html = doc.select("html").first();
            String htmlText = html.outerHtml();
            Page page = new Page();
            page.setContent(htmlText);
            page.setPath(site.getUrl());
            page.setCode(responceCode);
            page.setSite(site);
            pageRepository.save(page);

            Map<String, Integer> lemmas = lemmas(htmlText);
            Set<String> keys = lemmas.keySet();
            for (String key : keys) {
                Lemma oldLemma = lemmaRepository.findLemmaByLemma(key);
                if (oldLemma != null) {
                    int newFrequency = oldLemma.getFrequency() + 1;
                    oldLemma.setFrequency(newFrequency);
                    lemmaRepository.save(oldLemma);
                    continue;
                }
                Lemma lemma = new Lemma();
                lemma.setLemma(key);
                lemma.setSite(site);
                lemma.setSiteId(site.getId());
                lemma.setFrequency(1);
                lemmaRepository.save(lemma);

                Index index = new Index();
                index.setPage(page);
                index.setLemma(lemma);
                index.setPageId(page.getId());
                index.setLemmaId(lemma.getId());
                int rank = lemmas.get(key);
                index.setRank(rank);
                indexRepository.save(index);
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
        Pattern pattern = Pattern.compile("[а-я]+", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        for (String word : words) {
            if(word.length() < 1 || !pattern.matcher(word).matches()){
                continue;
            }
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

    private boolean ifKirillic(String text){
        Pattern pattern = Pattern.compile("[а-я]+", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        return pattern.matcher(text).find();
    }
}
