package searchengine.utils.indexing;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.config.SitesList;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.enums.StatusEnum;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SitesRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import static searchengine.utils.indexing.IndexingServiceImpl.ifStopped;

public class IndexingMultithread extends RecursiveTask<List<Page>>{
    private Site site;

    private SitesList sitesList;
    private SitesRepository sitesRepository;
    private PageRepository pageRepository;
    Connection.Response response;

    private String link;

    private String statusTime = LocalDateTime.now().toString();

    private int statusCode;
    private String lastError;

    private ForkJoinPool pool;

    private volatile StatusEnum status;
    public IndexingMultithread(Site site, SitesList sitesList, String link, SitesRepository sitesRepository, PageRepository pageRepository) {
        this.site = site;
        this.sitesList = sitesList;
        this.link = link;
        this.sitesRepository = sitesRepository;
        this.pageRepository = pageRepository;
    }

    @Autowired
    public IndexingMultithread(SitesList sitesList){
        this.sitesList = sitesList;
    }
    //реализовать лемматизацию каждой страницы

    @Override
    protected List<Page> compute(){
        List<Page> subsites = new ArrayList<>();
        List<IndexingMultithread> tasks = new ArrayList<>();

        //В случае, если переданная страница уже была проиндексирована, перед её индексацией необходимо удалить всю информацию о ней из таблиц page, lemma и index.
        //Коды страниц, при получении которых HTTP-ответ был ошибочным (с кодами 4xx или 5xx), индексировать не нужно.


        try {
            Thread.sleep((long)(500 + Math.random() * 4500));
                 response = Jsoup.connect(link).userAgent("Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36").execute();
                 statusCode = response.statusCode();

                Document document = Jsoup.connect(link).userAgent("Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36").get();
                Elements links = document.select("a");
                if (!links.isEmpty()) {
                    links.forEach(l -> {
                        Page page = new Page();
                        String path = l.attr("abs:href");
                        if(!ifMatchesLinkForm(path) || ifEqualsSiteUrl(path) || !isLinkSupportedType(path)){
                            return;
                        }
                        String cutPathString = cutPath(path);
                        if(isFollowed(cutPathString)){
                            return;
                        }
                        try {
                            Document doc = Jsoup.connect(path).userAgent("Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36").get();
                            Element allPageCode = doc.select("html").first();
                            String content = allPageCode.outerHtml();
                            page.setContent("'" + content + "'");
                        } catch (IOException ex){
                            ex.printStackTrace();
                        }
                        page.setCode(statusCode);
                        page.setPath(cutPathString);
                        page.setSite(site);
                        page.setSiteId(site.getId());
                        pageRepository.save(page);
                        site.setStatusTime(statusTime);
                        sitesRepository.save(site);

                        if(ifStopped){
                            status = StatusEnum.FAILED;
                            site.setStatus(status);
                            site.setStatusTime(statusTime);
                            sitesRepository.save(site);
                        }

                        if(!ifStopped) {
                            IndexingMultithread task = new IndexingMultithread(site, sitesList, path, sitesRepository, pageRepository);
                            task.fork();
                            tasks.add(task);
                        }
/*                        List<IndexingMultithread> sitesIndexing = sitesList.getSitesTasks();
                        for(IndexingMultithread t : sitesIndexing){
                            isIndexed(t);
                        }*/
                    });
                }
                for (IndexingMultithread task : tasks) {
                   // pageRepository.saveAll(task.join());
                    subsites.addAll(task.join());
                }
            } catch(Exception exception){

            //ПЕРЕДЕЛАТЬ, ВСТАВЛЯТЬ В last_error ошибку только если индексирование сайта ПРЕРВАНО
            exception.printStackTrace();
        }
        site.setStatusTime(statusTime);
/*        if(site.getStatus() == StatusEnum.INDEXING && !pool.isShutdown()){
            site.setStatus(StatusEnum.INDEXED);
        }*/
        sitesRepository.save(site);
        return subsites;
    }

    private boolean isFollowed(String path){
        Optional<Page> followedPage = pageRepository.findFirstPageByPath(path);
        return !followedPage.isEmpty();
    }

    private String cutPath(String path) {
        String cutPath = path.substring(site.getUrl().length() - 1, path.length());
        return cutPath;
    }

    private boolean ifMatchesLinkForm(String path){
        boolean matchesLinkForm = false;
        String regex = "http://";
        String secondRegex = "https://";
        if (path.contains(regex) || path.contains(secondRegex) && path.contains(link)) {
            matchesLinkForm = true;
        }
        return matchesLinkForm;
    }

    private boolean ifEqualsSiteUrl(String path){
        boolean equalsSiteUrl = path.equals(link);
        return equalsSiteUrl;
    }

    private void isIndexed(IndexingMultithread task){
        Site site = task.getSite();
        if (task.isCompletedNormally()) {
            site.setStatus(StatusEnum.INDEXED);
            site.setStatusTime(statusTime.toString());
            site.setLastError("");
            sitesRepository.save(site);
        } else if (task.isCompletedAbnormally()) {
            site.setStatus(StatusEnum.FAILED);
            site.setStatusTime(statusTime.toString());
            site.setLastError("Индексирование прекращено пользователем");
            sitesRepository.save(site);
        }
    }

    private boolean isLinkSupportedType(String path){
        String type = path.substring(path.length()-4, path.length());
        if(type.matches(".jpg")){
            return false;
        }
        return true;
    }

    public Site getSite(){
        return site;
    }

}

