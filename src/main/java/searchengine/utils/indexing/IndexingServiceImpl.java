package searchengine.utils.indexing;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.enums.StatusEnum;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SitesRepository;
import searchengine.services.indexing.IndexingService;
import searchengine.utils.indexing.IndexingMultithread;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;


@Component
public class IndexingServiceImpl implements IndexingService {

    private final String IS_INDEXING = "Индексация уже запущена";
    private final String NOT_INDEXING = "Индексация не запущена";
    private final String INDEXING_STOPPED = "Индексация остановлена пользователем";
    private final String INDEXING_CANNOT_BE_STARTED = "Индексация не может быть запущена сейчас";
    private final String INDEXING_TERMINATING = "Индексация останавливается";
    private final String INDEXING_ONE_PAGE_ERROR_DOESNT_MATCH_LINK_FORM = "Некорректная ссылка";
    private final String INDEXING_ONE_PAGE_ERROR_SITE_NOT_FOUND = "Данная страница находится за пределами сайтов, указанных в конфигурационном файле";


    private final String HTTPS_STRING = "https://";
    private final String HTTP_STRING = "http://";
    private final SitesList sitesList;
    private volatile ForkJoinPool pool = new ForkJoinPool();
    private SitesRepository sitesRepository;
    private PageRepository pageRepository;
    private LemmaRepository lemmaRepository;
    private IndexRepository indexRepository;

    public static boolean ifStopped = false;
    Connection.Response response;

    private String statusTime = LocalDateTime.now().toString();

    public IndexingServiceImpl(SitesList sitesList, SitesRepository sitesRepository, PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository){
        this.sitesList = sitesList;
        this.sitesRepository = sitesRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
    }

    public IndexingResponse startIndexing(){
        IndexingResponse indexingResponse = new IndexingResponse();
        if(!ifPoolIsAllowedToStartIndexing()){
            indexingResponse.setError(INDEXING_CANNOT_BE_STARTED);
            indexingResponse.setResult(false);
            return indexingResponse;
        }
        lemmaRepository.deleteAll();
        indexRepository.deleteAll();

        List<Site> sites = sitesList.getSites();
        for(Site site : sites){
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
            pool.execute(indexingMultithread);
        }
        indexingResponse.setResult(true);
        return indexingResponse;
    }

    @Transactional
    public IndexingResponse stopIndexing(){
        IndexingResponse indexingResponse = new IndexingResponse();
        ifStopped = true;
        indexingResponse.setResult(true);
        return indexingResponse;
    }

    private boolean ifPoolIsAllowedToStartIndexing(){
        boolean isAllowed = true;
        if(this.pool.isTerminating() || this.pool.getActiveThreadCount() != 0){
            isAllowed = false;
        }
        return isAllowed;
    }

    private void clearDataBase(Site site){
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

    private int getCode (Site site) throws IOException{
        response = Jsoup.connect(site.getUrl()).userAgent("Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36").execute();
        int statusCode = response.statusCode();
        return statusCode;
    }


    //старая версия метода остановки индексации
/*    @Override
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
    }*/
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
