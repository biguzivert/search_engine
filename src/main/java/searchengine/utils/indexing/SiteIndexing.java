package searchengine.utils.indexing;

import org.springframework.beans.factory.annotation.Autowired;
import searchengine.config.SitesList;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.enums.StatusEnum;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SitesRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

import static searchengine.utils.indexing.IndexingServiceImpl.getCode;

public class SiteIndexing implements Runnable{

    private searchengine.config.Site site;
    private SitesRepository sitesRepository;
    private PageRepository pageRepository;
    private SitesList sitesList;
    private String statusTime = LocalDateTime.now().toString();

    public SiteIndexing(){};
    public SiteIndexing(searchengine.config.Site site, SitesRepository sitesRepository, PageRepository pageRepository, SitesList sitesList){
        this.site = site;
        this.sitesRepository = sitesRepository;
        this.pageRepository = pageRepository;
        this.sitesList = sitesList;
    }
    @Override
    public void run() {
        Set<Page> pageSet = new HashSet<>();
        clearDataBase(site);
        searchengine.model.Site newSite = new searchengine.model.Site(site.getName(), StatusEnum.INDEXING, site.getUrl(), statusTime);
        try {
            String lastError = getCode(site) != 200 ? "Ошибка индексации: главная страница сайта недоступна" : "";
            newSite.setLastError(lastError);
        } catch (IOException ex){
            ex.printStackTrace();
        }
        sitesRepository.save(newSite);
        String link = newSite.getUrl();
        IndexingMultithread indexingMultithread = new IndexingMultithread(newSite, sitesList,  link, sitesRepository, pageRepository);
        List<Page> pages= new ForkJoinPool().invoke(indexingMultithread);
        //когда pages.size() == 20 (например), сохранять данные в pageRepository и обнулять pages
        pageSet.addAll(pages);
        pageRepository.saveAll(pageSet);
        newSite.setStatusTime(statusTime);
        newSite.setStatus(StatusEnum.INDEXED);
        sitesRepository.save(newSite);
    }

    private void clearDataBase(searchengine.config.Site site){
        List<searchengine.model.Site> sitesToDelete = sitesRepository.findAllSitesByUrl(site.getUrl());
        if(sitesToDelete == null){
            return;
        }
        for(searchengine.model.Site s : sitesToDelete){
            int siteId = s.getId();
            sitesRepository.deleteAllSitesById(siteId);
            pageRepository.deletePagesBySiteId(siteId);
        }
    }
}
