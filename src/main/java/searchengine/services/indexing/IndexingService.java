package searchengine.services.indexing;

import searchengine.dto.indexing.IndexingResponse;

public interface IndexingService {
    IndexingResponse startIndexing();

    IndexingResponse stopIndexing();

    IndexingResponse indexOnePage(String url);
}
