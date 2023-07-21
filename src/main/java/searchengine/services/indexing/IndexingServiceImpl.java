package searchengine.services.indexing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.enums.StatusEnum;
import searchengine.services.repositories.PageRepository;
import searchengine.services.repositories.SitesRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Service
public class IndexingServiceImpl implements IndexingService{

    private final String IS_INDEXING = "Индексация уже запущена";
    private final String NOT_INDEXING = "Индексация не запущена";
    private final String INDEXING_STOPPED = "Индексация остановлена пользователем";
    private final SitesList sitesList;

    private IndexingMultithread indexingMultithread;
    private ForkJoinPool pool = new ForkJoinPool();
    private SitesRepository sitesRepository;
    private PageRepository pageRepository;

    private String statusTime = LocalDateTime.now().toString();

    public IndexingServiceImpl(SitesList sitesList, SitesRepository sitesRepository, PageRepository pageRepository){
        this.sitesList = sitesList;
        this.sitesRepository = sitesRepository;
        this.pageRepository = pageRepository;
    }

    public IndexingResponse startIndexing(){
        IndexingResponse indexingResponse = new IndexingResponse();
        if(pool.getActiveThreadCount() != 0){
            indexingResponse.setError(IS_INDEXING);
            indexingResponse.setResult(false);
        } else
        {
            List<Site> sites = sitesList.getSites();
            for(Site site : sites){
                searchengine.model.Site siteToDelete = sitesRepository.findSiteByUrl(site.getUrl());
                if(siteToDelete != null){
                    sitesRepository.delete(siteToDelete);
                    pageRepository.deleteSiteById(siteToDelete.getId());
                }
                searchengine.model.Site newSite = new searchengine.model.Site();
                newSite.setName(site.getName());
                newSite.setStatus(StatusEnum.INDEXING);
                newSite.setUrl(site.getUrl());
                newSite.setStatusTime(statusTime);
                newSite.setLastError("");
                sitesRepository.save(newSite);
                String link = newSite.getUrl();
                pool.invoke(new IndexingMultithread(newSite, link));
            }
            indexingResponse.setResult(true);
        }
        return indexingResponse;
    }

    @Override
    public IndexingResponse stopIndexing() {
        IndexingResponse indexingResponse = new IndexingResponse();
        if(pool.getActiveThreadCount() == 0){
            indexingResponse.setError(NOT_INDEXING);
            indexingResponse.setResult(false);
        } else {
            pool.shutdown();
            sitesRepository.updateStatusAndError(StatusEnum.INDEXING, StatusEnum.FAILED, INDEXING_STOPPED);
            indexingResponse.setResult(true);
        }
        return indexingResponse;
    }
}
