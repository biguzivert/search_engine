package searchengine.services.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;
import searchengine.config.SitesList;
import searchengine.dto.search.SearchResponse;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.repositories.IndexRepository;
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

    private IndexRepository indexRepository;

    @Autowired
    public SearchServiceImpl(SitesList sitesList, SitesRepository sitesRepository, LemmaRepository lemmaRepository, PageRepository pageRepository, IndexRepository indexRepository){
        this.sitesList = sitesList;
        this.sitesRepository = sitesRepository;
        this.lemmaRepository = lemmaRepository;
        this.pageRepository = pageRepository;
    }
    @Override
    public SearchResponse search(String query, String site){
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
        Site siteDB = sitesRepository.findSiteByUrl(site);
        List<Page> pages = pageRepository.findPagesBySiteId(siteDB.getId());
        int pagesCount = pages.size();

        ArrayList<searchengine.config.Lemma> lemmasWithFrequency = new ArrayList<>();
        for(String k : keys){
            Lemma l = lemmaRepository.findLemmaByLemma(k);
            if(l == null){
                continue;
            }
            //возможно в таблице будет несколько одинаковых лемм с разными siteId, что выдаст ошибку на строке 53 при запросе леммы из базы по лемме,
            //и что искажает реальное количество страниц на которых встречается лемма
            int lemmaCount = l.getFrequency();
            if((lemmaCount / pagesCount) >= 0.4){
                lemmas.remove(k);
                continue;
            }
                searchengine.config.Lemma lemmaWithFrequency = new searchengine.config.Lemma(k, lemmaCount);
                lemmasWithFrequency.add(lemmaWithFrequency);
        }
        Collections.sort(lemmasWithFrequency);
        for(searchengine.config.Lemma l : lemmasWithFrequency){

        }

        return searchResponse;
    }

    //По первой, самой редкой лемме из списка, находить все страницы, на которых она встречается. Далее искать соответствия
    // следующей леммы из этого списка страниц, а затем повторять операцию по каждой следующей лемме.
    // Список страниц при этом на каждой итерации должен уменьшаться.
    private float relevancy(List<searchengine.config.Lemma> lemmas){
        float relevancy = 0;
            Lemma firstLemma = lemmaRepository.findLemmaByLemma(lemmas.get(0).getLemma());
            int firstLemmaId = firstLemma.getId();
            List<Page> pages = indexRepository.findPagesByLemmaId(firstLemmaId);
        if(pages.size() != 0){
            float rankAbsolute = 0;
            for(searchengine.config.Lemma l : lemmas){
                Lemma lemmaToCalcAbsRank = lemmaRepository.findLemmaByLemma(l.getLemma());
                for(Page p : pages){
                    rankAbsolute = rankAbsolute + indexRepository.findRankByLemmaIdOnPage(lemmaToCalcAbsRank.getId(), p.getId());
                }
            }
                Lemma secondLemma = lemmaRepository.findLemmaByLemma(lemmas.get(1).getLemma());
                int secondLemmaId = secondLemma.getId();
                for(Page p : pages){
                    float rankFirstLemma = indexRepository.findRankByLemmaIdOnPage(firstLemmaId, p.getId());
                    float rankSecondLemma = indexRepository.findRankByLemmaIdOnPage(secondLemmaId, p.getId());
                    float rAbs = rankFirstLemma + rankSecondLemma;
                }
            }

        return relevancy;
    }
}
