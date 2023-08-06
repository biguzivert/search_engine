package searchengine.services.indexing;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.enums.StatusEnum;
import searchengine.services.repositories.PageRepository;
import searchengine.services.repositories.SitesRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class IndexingMultithread extends RecursiveTask<List<Page>>{
    private Site site;
//    private Page page;
    //по идее репозиторий возвращает Optional - уточнить
    private SitesRepository sitesRepository;
    private PageRepository pageRepository;
    Connection.Response response;

    private String link;

    private String statusTime = LocalDateTime.now().toString();

    private int statusCode;
    private String lastError;

    private ForkJoinPool pool;

    public IndexingMultithread(Site site, String link, SitesRepository sitesRepository, PageRepository pageRepository) {
        this.site = site;
        this.link = link;
        this.sitesRepository = sitesRepository;
        this.pageRepository = pageRepository;
    }
    //надо создать класс с конструктором при вызове которого в indexingserviceiml запускается отдельный поток с проверкой именно этого

    @Override
    protected List<Page> compute(){
        List<Page> subsites = new ArrayList<>();
        List<IndexingMultithread> tasks = new ArrayList<>();

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
                        if(!ifMatchesLinkForm(path) || ifEqualsSiteUrl(path)){
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
                            page.setContent(content);
                        } catch (IOException ex){
                            ex.printStackTrace();
                        }
                        page.setCode(statusCode);
                        page.setPath(cutPathString);
                        page.setSite(site);
                        page.setSiteId(site.getId());
                        pageRepository.save(page);
                        site.setStatusTime(statusTime);
                        site.setStatus(StatusEnum.INDEXING);
                        sitesRepository.save(site);

                        IndexingMultithread task = new IndexingMultithread(site, path, sitesRepository, pageRepository);
                        task.fork();
                        tasks.add(task);


                    });
                }
                for (IndexingMultithread task : tasks) {
                    subsites.addAll(task.join());
                }
            } catch(Exception exception){

            //ПЕРЕДЕЛАТЬ, ВСТАВЛЯТЬ В last_error ошибку только если индексирование сайта ПРЕРВАНО
            site.setStatusTime(statusTime);
            site.setStatus(StatusEnum.FAILED);
            lastError = exception.getMessage();
            site.setLastError(lastError);
            sitesRepository.save(site);
        }
        site.setStatusTime(statusTime);
        if(site.getStatus() == StatusEnum.INDEXING && !pool.isShutdown()){
            site.setStatus(StatusEnum.INDEXED);
        }
        sitesRepository.save(site);
        return subsites;
    }

    private boolean isFollowed(String path){
        boolean ifFollowed = pageRepository.findPageByPath(path) != null;
        return ifFollowed;
    }

    private String cutPath(String path) {
        String cutPath = path.substring(link.length(), path.length());
        return "/" + cutPath;
    }

    private boolean ifMatchesLinkForm(String path){
        boolean matchesLinkForm = false;
        String regex = "http://";
        String secondRegex = "https://";
        if (path.contains(regex) || path.contains(secondRegex)) {
            matchesLinkForm = true;
        }
        return matchesLinkForm;
    }

    private boolean ifEqualsSiteUrl(String path){
        boolean equalsSiteUrl = path.equals(link);
        return equalsSiteUrl;
    }

}

