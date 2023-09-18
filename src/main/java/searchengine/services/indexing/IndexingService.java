package searchengine.services.indexing;

import org.springframework.stereotype.Service;
import searchengine.dto.indexing.IndexingResponse;

public interface IndexingService {
    IndexingResponse startIndexing();

    IndexingResponse stopIndexing();

    IndexingResponse indexOnePage(String url);
}
