package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.IndexingResponse;
import searchengine.model.enums.StatusEnum;
import searchengine.services.repositories.PageRepository;
import searchengine.services.repositories.SitesRepository;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Service
public class IndexingServiceImpl implements IndexingService{

    private final SitesList sitesList = new SitesList();

    private IndexingMultithread indexingMultithread;
    @Override
    public IndexingResponse startIndexing() {

        List<Site> sites = sitesList.getSites();

        new ForkJoinPool().invoke(new IndexingMultithread(sites));


        

        return null;
    }

    @Override
    public IndexingResponse stopIndexing() {
        return null;
    }
}
