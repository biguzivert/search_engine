package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.IndexingResponse;
import searchengine.model.Page;
import searchengine.model.enums.StatusEnum;
import searchengine.services.repositories.PageRepository;
import searchengine.services.repositories.SitesRepository;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Service
public class IndexingServiceImpl implements IndexingService{

    private final SitesList sitesList = new SitesList();

    private IndexingMultithread indexingMultithread;
    private ForkJoinPool pool;
    private SitesRepository sitesRepository;
    private PageRepository pageRepository;

    public IndexingResponse startIndexing(){
        List<Site> sites = sitesList.getSites();
        for(Site site : sites){
            searchengine.model.Site siteToDelete = sitesRepository.findSiteByUrl(site.getUrl());
            sitesRepository.delete(siteToDelete);
            pageRepository.deleteSite(siteToDelete);
            searchengine.model.Site newSite = new searchengine.model.Site();
            newSite.setName(site.getName());
            newSite.setStatus(StatusEnum.INDEXING);
            newSite.setUrl(site.getUrl());
            sitesRepository.save(newSite);
            String link = newSite.getUrl();
            pool.invoke(new IndexingMultithread(newSite, link));
        }

        return null;
    }

    @Override
    public IndexingResponse stopIndexing() {
        return null;
    }
}
