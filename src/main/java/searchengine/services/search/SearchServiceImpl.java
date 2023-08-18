package searchengine.services.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;
import searchengine.config.SitesList;
import searchengine.dto.search.SearchResponse;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.repositories.LemmaRepository;
import searchengine.model.repositories.PageRepository;
import searchengine.model.repositories.SitesRepository;
import searchengine.services.lemmatization.Lemmatization;

import java.io.IOException;
import java.util.*;

public class SearchServiceImpl implements SearchService{

    private final SitesList sitesList;
    private SitesRepository sitesRepository;
    private LemmaRepository lemmaRepository;
    private PageRepository pageRepository;

    @Autowired
    public SearchServiceImpl(SitesList sitesList, SitesRepository sitesRepository, LemmaRepository lemmaRepository, PageRepository pageRepository){
        this.sitesList = sitesList;
        this.sitesRepository = sitesRepository;
        this.lemmaRepository = lemmaRepository;
        this.pageRepository = pageRepository;
    }
    @Override
    public SearchResponse search(String query){
        Lemmatization lemmatizator = new Lemmatization();
        SearchResponse searchResponse = new SearchResponse();
        Map<String, Integer> lemmas = new HashMap<>();
        try {
            lemmas = lemmatizator.lemmas(query);
        } catch (IOException ex){
            ex.printStackTrace();
        }

        //Исключать из полученного списка леммы, которые встречаются на слишком большом количестве страниц. Поэкспериментируйте и определите этот процент самостоятельно.
        Set<String> keys = lemmas.keySet();
        Iterable<Site> sites = sitesRepository.findAll();
        int pagesCount = 0;
        for(Site s : sites){
            List<Page> pages = pageRepository.findPagesBySiteId(s.getId());
            pagesCount = pagesCount + pages.size();
        }
        for(String k : keys){
            Lemma l = lemmaRepository.findLemmaByLemma(k);
            if(l == null){
                continue;
            }
            //возможно в таблице будет несколько одинаковых лемм с разными siteId, что выдаст ошибку на строке 53 при запросе леммы из базы по лемме,
            //и что искажает реальное количество страниц на которых встречается лемма
            int lemmaCount = l.getFrequency();
            if((lemmaCount / pagesCount) >= 0.7){
                lemmas.remove(k);
            }
        }


        return searchResponse;
    }
}
