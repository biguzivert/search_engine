package searchengine.services.search;

import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;


public interface SearchService {

    SearchResponse search(String query, String site);
}
