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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class IndexingMultithread extends RecursiveTask<List<Page>> {
    private Site site;
    private Page page;
    private SitesRepository sitesRepository;
    private PageRepository pageRepository;
    Connection.Response response;

    private String link;

    private String statusTime = LocalDateTime.now().toString();

    private int statusCode;
    private String lastError;

    public IndexingMultithread(Site site, String link) {
        this.site = site;
        this.link = link;
    }

    @Override
    protected List<Page> compute() {
        List<Page> subsites = new ArrayList<>();
        List<IndexingMultithread> tasks = new ArrayList<>();

        try {
            Thread.sleep(1000);
                 response = Jsoup.connect(link).userAgent("Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36").execute();
                 statusCode = response.statusCode();

                Document document = Jsoup.connect(link).userAgent("Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36").get();
                Elements links = document.select("a");
                if (!links.isEmpty()) {
                    links.forEach(l -> {
                        Page page = new Page();
                        String path = l.attr("abs:href");
                        String cutPath = cutPath(path);
                        //спросить можно ли return
                        if(isFollowed(cutPath) == true){
                            return;
                        }
                        Element allPageCode = l.select("html").first();
                        String content = allPageCode.outerHtml();

                        page.setCode(statusCode);
                        page.setPath(cutPath);
                        page.setSiteId(site.getId());
                        page.setContent(content);
                        site.setStatusTime(statusTime);
                        site.setStatus(StatusEnum.INDEXING);

                        IndexingMultithread task = new IndexingMultithread(site, path);
                        task.fork();
                        tasks.add(task);


                    });
                }
                for (IndexingMultithread task : tasks) {
                    subsites.addAll(task.join());
                }
            } catch(Exception exception){
            site.setStatusTime(statusTime);
            statusCode = response.statusCode();
            page.setCode(statusCode);
            site.setStatus(StatusEnum.FAILED);
            lastError = exception.getMessage();
        }
        site.setStatusTime(statusTime);
        site.setStatus(StatusEnum.INDEXED);
        return subsites;
    }

    private boolean isFollowed(String path){
        boolean ifFollowed = false;
        if(pageRepository.findPageByPath(path) != null){
            ifFollowed = true;
        }
        return ifFollowed;
    }

    private String cutPath(String path) {
        int deleteTo = path.lastIndexOf(link);
        String cutPath = path.substring(deleteTo, path.length());
        return cutPath;
    }
}

