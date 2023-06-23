package searchengine.services;

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

public class IndexingMultithread extends RecursiveTask<Site> {
    private Site site;
    private Page page;
    private SitesRepository sitesRepository;
    private PageRepository pageRepository;

    private String link;

    private String statusTime;

    public IndexingMultithread(Site site, String link) {
        this.site = site;
        this.link = link;
    }

 /*   private IndexingMultithread(Site site, Page page){
        this.site = site;
        this.page = page;
    }*/

    @Override
    protected Site compute() {
        List<Site> subsites = new ArrayList<>();
        List<IndexingMultithread> tasks = new ArrayList<>();
        try {
            Document document = Jsoup.connect(link).userAgent("Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36").get();
            Elements links = document.select("a");
            if (!links.isEmpty()) {
                links.forEach(l -> {
                    Page page = new Page();
                    String path = l.attr("abs:href");
                    Element allPageCode = l.select("html").first();
                    String content = allPageCode.outerHtml();
                    statusTime = LocalDateTime.now().toString();

                    //получить код для Code Page

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
                subsites.add(task.join());
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    //должен возвращать pages, соотвественно надо изменить тип данных метода
        return null;
    }
}


    /*
    @Override
    protected Site compute() {

        List<IndexingMultithread> taskList = new ArrayList<>();
        sites.forEach(s -> {
            taskList.add(new IndexingMultithread(s));
            searchengine.model.Site site = sitesRepository.findSiteByUrl(s.getUrl());
            sitesRepository.deleteById(site.getId());
            pageRepository.deleteSiteBySiteId(site.getId());

            searchengine.model.Site newSite = new searchengine.model.Site();
            newSite.setName(s.getName());
            newSite.setUrl(s.getUrl());
            newSite.setStatus(StatusEnum.INDEXING);

            try {
                Document document = Jsoup.connect(site.getUrl()).userAgent("Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36")
                        .get();


            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
*/


