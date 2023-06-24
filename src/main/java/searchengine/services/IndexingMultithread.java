package searchengine.services;

import org.apache.catalina.connector.Response;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.Page;
import searchengine.model.Site;
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

    private String statusTime;

    private int statusCode;

    public IndexingMultithread(Site site, String link) {
        this.site = site;
        this.link = link;
    }

    @Override
    protected List<Page> compute() {
        List<Page> subsites = new ArrayList<>();
        List<IndexingMultithread> tasks = new ArrayList<>();

        try {
                 response = Jsoup.connect(link).userAgent("Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36").execute();
                 statusCode = response.statusCode();

                Document document = Jsoup.connect(link).userAgent("Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36").get();
                Elements links = document.select("a");
                if (!links.isEmpty()) {
                    links.forEach(l -> {
                        Page page = new Page();
                        String path = l.attr("abs:href");
                        Element allPageCode = l.select("html").first();
                        String content = allPageCode.outerHtml();
                        statusTime = LocalDateTime.now().toString();

                        page.setCode(statusCode);
                        page.setPath(path);
                        page.setSiteId(site.getId());
                        page.setContent(content);
                        site.setStatusTime(statusTime);

                        IndexingMultithread task = new IndexingMultithread(site, path);
                        task.fork();
                        tasks.add(task);


                    });
                }
                for (IndexingMultithread task : tasks) {
                    subsites.addAll(task.join());
                }
            } catch(Exception exception){
            exception.printStackTrace();
        }
        return subsites;
    }
}

