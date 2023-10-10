package searchengine.services.indexing;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.Index;
import searchengine.model.enums.StatusEnum;
import searchengine.model.repositories.PageRepository;
import searchengine.model.repositories.SitesRepository;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.FutureTask;

@Service
public class IndexingServiceImpl implements IndexingService{

    private final String IS_INDEXING = "Индексация уже запущена";
    private final String NOT_INDEXING = "Индексация не запущена";
    private final String INDEXING_STOPPED = "Индексация остановлена пользователем";
    private final String INDEXING_TERMINATING = "Индексация останавливается";

    private final String INDEXING_ONE_PAGE_ERROR_DOESNT_MATCH_LINK_FORM = "Некорректная ссылка";
    private final String INDEXING_ONE_PAGE_ERROR_SITE_NOT_FOUND = "Данная страница находится за пределами сайтов, указанных в конфигурационном файле";


    private final String HTTPS_STRING = "https://";
    private final String HTTP_STRING = "http://";
    private final SitesList sitesList;
    private volatile ForkJoinPool pool = new ForkJoinPool();
    private SitesRepository sitesRepository;
    private PageRepository pageRepository;
    Connection.Response response;

    private String statusTime = LocalDateTime.now().toString();

    public IndexingServiceImpl(SitesList sitesList, SitesRepository sitesRepository, PageRepository pageRepository){
        this.sitesList = sitesList;
        this.sitesRepository = sitesRepository;
        this.pageRepository = pageRepository;
    }

    public IndexingResponse startIndexing(){
        IndexingResponse indexingResponse = new IndexingResponse();
        List<IndexingMultithread> tasks = new ArrayList<>();
        if(pool.isTerminating()){
            indexingResponse.setError(INDEXING_TERMINATING);
            indexingResponse.setResult(false);
            return indexingResponse;
        }
        if(pool.getActiveThreadCount() != 0){
            indexingResponse.setError(IS_INDEXING);
            indexingResponse.setResult(false);
            return indexingResponse;
        }
        if(pool.isShutdown() && pool.isTerminated()){

            this.pool = new ForkJoinPool();
        }

            List<Site> sites = sitesList.getSites();
            for(Site site : sites){
                    List<searchengine.model.Site> sitesToDelete = sitesRepository.findAllSitesByUrl(site.getUrl());
                    if(sitesToDelete != null){
                        List<Integer> siteIds = new ArrayList<>();
                        for(searchengine.model.Site s : sitesToDelete){
                            siteIds.add(s.getId());
                        }
                        sitesRepository.deleteAllSitesByUrl(site.getUrl());
                        siteIds.forEach(s -> pageRepository.deleteSiteById(s));
                    }
                searchengine.model.Site newSite = new searchengine.model.Site();
                newSite.setName(site.getName());
                newSite.setStatus(StatusEnum.INDEXING);
                newSite.setUrl(site.getUrl());
                newSite.setStatusTime(statusTime);

                String lastError = "";
                try {
                    response = Jsoup.connect(site.getUrl()).userAgent("Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36").execute();
                    int statusCode = response.statusCode();
                    if (statusCode != 200) {
                        lastError = "Ошибка индексации: главная страница сайта недоступна";
                    }
                } catch (IOException ex){
                    ex.printStackTrace();
                }
                newSite.setLastError(lastError);
                sitesRepository.save(newSite);
                String link = newSite.getUrl();
                IndexingMultithread indexingMultithread = new IndexingMultithread(newSite, sitesList,  link, sitesRepository, pageRepository);
                List<IndexingMultithread> tasksInConfig = sitesList.getSitesTasks();
                if(tasksInConfig != null){
                    tasks.addAll(tasksInConfig);
                }
                tasks.add(indexingMultithread);
                sitesList.setSitesTasks(tasks);
                pool.execute(indexingMultithread);
            }

            indexingResponse.setResult(true);
        //tasks.forEach(t -> pool.execute(new IndexingCheck(t, t.getSite(), sitesRepository)));
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
            List<IndexingMultithread> sitesTasks= sitesList.getSitesTasks();
            for(IndexingMultithread t : sitesTasks){
                searchengine.model.Site s = t.getSite();
                if(!t.isDone()){
                    s.setLastError("Индексация остановлена пользователем");
                    s.setStatus(StatusEnum.FAILED);
                    s.setStatusTime(statusTime);
                    sitesRepository.save(s);
                }
            }
            pool.shutdownNow();
            indexingResponse.setResult(true);
        }
        return indexingResponse;
    }
    public IndexingResponse indexOnePage(String url){
        IndexingResponse indexingResponse = new IndexingResponse();
        if(pool.isTerminating()) {
            indexingResponse.setError(INDEXING_TERMINATING);
            indexingResponse.setResult(false);
            return indexingResponse;
        }
        if(pool.getActiveThreadCount() != 0){
                indexingResponse.setError(IS_INDEXING);
                indexingResponse.setResult(false);
                return indexingResponse;
        }
        if(pool.isShutdown() && pool.isTerminated()){
            this.pool = new ForkJoinPool();
        }

                int indexOfTransferProtocole = url.indexOf(HTTP_STRING) != 0 ? url.indexOf(HTTP_STRING) : url.indexOf(HTTPS_STRING);
                if(indexOfTransferProtocole == 0){
                    indexingResponse.setResult(false);
                    indexingResponse.setError(INDEXING_ONE_PAGE_ERROR_DOESNT_MATCH_LINK_FORM);
                    return indexingResponse;
                }

                Iterable<searchengine.model.Site> sites = sitesRepository.findAll();
                searchengine.model.Site siteToIndex = null;
                boolean ifPageLinkedToSite = false;
                for(searchengine.model.Site s : sites){
                    if(url.contains(s.getUrl())){
                        siteToIndex = s;
                        ifPageLinkedToSite = true;
                        break;
                    }
                }
                if(ifPageLinkedToSite == false){
                 indexingResponse.setResult(false);
                 indexingResponse.setError(INDEXING_ONE_PAGE_ERROR_SITE_NOT_FOUND);
                 return indexingResponse;
                }
                pool.execute(new IndexingMultithread(siteToIndex, sitesList,  url, sitesRepository, pageRepository));
            indexingResponse.setResult(true);
        return indexingResponse;
    }
}
