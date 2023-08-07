package searchengine.services.indexing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.enums.StatusEnum;
import searchengine.services.repositories.PageRepository;
import searchengine.services.repositories.SitesRepository;

import javax.persistence.NonUniqueResultException;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Service
public class IndexingServiceImpl implements IndexingService{

    private final String IS_INDEXING = "Индексация уже запущена";
    private final String NOT_INDEXING = "Индексация не запущена";
    private final String INDEXING_STOPPED = "Индексация остановлена пользователем";
    private final String INDEXING_TERMINATING = "Индексация останавливается";
    private final SitesList sitesList;
    private volatile ForkJoinPool pool = new ForkJoinPool();
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
        if(pool.isTerminating()){
            indexingResponse.setError(INDEXING_TERMINATING);
            indexingResponse.setResult(false);
            return indexingResponse;
        }
        if(pool.isShutdown() && pool.isTerminated()){

            this.pool = new ForkJoinPool();
        }
        if(pool.getActiveThreadCount() != 0){
            indexingResponse.setError(IS_INDEXING);
            indexingResponse.setResult(false);
            return indexingResponse;
        } else
        {


            List<Site> sites = sitesList.getSites();
            for(Site site : sites){
                    List<searchengine.model.Site> sitesToDelete = sitesRepository.findAllSitesByUrl(site.getUrl());
                    if(sitesToDelete != null){
/*                        List<Integer> siteIds = new ArrayList<>();
                        for(searchengine.model.Site s : sitesToDelete){
                            siteIds.add(s.getId());
                        }*/
                        sitesRepository.deleteAllSitesByUrl(site.getUrl());
//                        siteIds.forEach(s -> pageRepository.deleteSiteById(s));
                    }
                searchengine.model.Site newSite = new searchengine.model.Site();
                newSite.setName(site.getName());
                newSite.setStatus(StatusEnum.INDEXING);
                newSite.setUrl(site.getUrl());
                newSite.setStatusTime(statusTime);
                newSite.setLastError("");
                sitesRepository.save(newSite);
                String link = newSite.getUrl();
                pool.execute(new IndexingMultithread(newSite, link, sitesRepository, pageRepository));
       //         new IndexingCheck(pool, newSite, sitesRepository).start();
            }
            indexingResponse.setResult(true);
        }
        return indexingResponse;
    }

    @Override
    @Transactional
    public IndexingResponse stopIndexing() {
        IndexingResponse indexingResponse = new IndexingResponse();
        if(pool.getActiveThreadCount() == 0){
            indexingResponse.setError(NOT_INDEXING);
            indexingResponse.setResult(false);
        } else {
            pool.shutdownNow();
            new Thread(() -> {
                try {
                    for (;;) {
                        if (pool.isTerminated()) {
                            sitesRepository.updateStatusAndError(StatusEnum.INDEXING, StatusEnum.FAILED, INDEXING_STOPPED);
                            break;
                        } else {
                            Thread.sleep(1000);
                            continue;
                        }
                    }
                } catch (InterruptedException ex){
                    ex.printStackTrace();
                }
            });
            indexingResponse.setResult(true);
        }
        return indexingResponse;
    }
    public IndexingResponse indexOnePage(String url){
        IndexingResponse indexingResponse = new IndexingResponse();
        if(pool.isTerminating()){
            indexingResponse.setError(INDEXING_TERMINATING);
            indexingResponse.setResult(false);
            return indexingResponse;
        }
        if(pool.isShutdown() && pool.isTerminated()){
            this.pool = new ForkJoinPool();
        }
        if(pool.getActiveThreadCount() != 0){
            indexingResponse.setError(IS_INDEXING);
            indexingResponse.setResult(false);
            return indexingResponse;
        } else
        {
                List<searchengine.model.Site> sitesToDelete = sitesRepository.findAllSitesByUrl(url);
                if(sitesToDelete != null){
/*                        List<Integer> siteIds = new ArrayList<>();
                        for(searchengine.model.Site s : sitesToDelete){
                            siteIds.add(s.getId());
                        }*/
                    sitesRepository.deleteAllSitesByUrl(url);
//                        siteIds.forEach(s -> pageRepository.deleteSiteById(s));
                }
                searchengine.model.Site newSite = new searchengine.model.Site();
                newSite.setName("ИМЯ");
                newSite.setStatus(StatusEnum.INDEXING);
                newSite.setUrl(url);
                newSite.setStatusTime(statusTime);
                newSite.setLastError("");
                sitesRepository.save(newSite);
                String link = newSite.getUrl();
                pool.execute(new IndexingMultithread(newSite, link, sitesRepository, pageRepository));
            indexingResponse.setResult(true);
        }
        return indexingResponse;
    }
}
