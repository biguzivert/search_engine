package searchengine.services.search;

import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;
import searchengine.dto.search.SearchResponse;
import searchengine.services.lemmatization.Lemmatization;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SearchServiceImpl implements SearchService{

    @Override
    public SearchResponse search(String query){
        Lemmatization lemmatizator = new Lemmatization();
        SearchResponse searchResponse = new SearchResponse();
       // Map<String, Integer> lemmas = lemmatizator.lemmas(query);

        return searchResponse;
    }
}
