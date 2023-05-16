package searchengine.services;

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

public class IndexingMultithread extends RecursiveTask<Site> {
    private searchengine.config.Site site;

    private Page page;
    private SitesRepository sitesRepository;
    private PageRepository pageRepository;

    private volatile String time;

    public IndexingMultithread(searchengine.config.Site site) {
        this.site = site;
    }

    private IndexingMultithread(searchengine.config.Site site, Page page){
        this.site = site;
        this.page = page;
    }

    @Override
    protected Site compute() {
        List<Site> subsites = new ArrayList<>();
        List<IndexingMultithread> tasks = new ArrayList<>();

        //перенести в немногопоточную часть в IndexingImpl, чтобы реализовать передачу Page в IndexingMultithread а не Site
        Site infoToDelete = sitesRepository.findSiteByUrl(site.getUrl());
        sitesRepository.delete(infoToDelete);
        pageRepository.deleteSite(infoToDelete);
        Site updateTableSite = new Site();
        updateTableSite.setStatus(StatusEnum.INDEXING);
        updateTableSite.setName(site.getName());
        updateTableSite.setUrl(site.getUrl());
        sitesRepository.save(updateTableSite);

        try {
            Document document = Jsoup.connect(site.getUrl()).userAgent("Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36").get();
            Elements links = document.select("a");
            if (!links.isEmpty()){
                links.forEach(l -> {
                    Page page = new Page();
                    String path = l.attr("abs:href");
                    Element allPageCode = l.select("html").first();
                    String content = allPageCode.outerHtml();
                    //получить корректный формат даты
                    time = LocalDateTime.now().toString();
                    //получить код для Code Page

                    page.setPath(path);
                    page.setSiteId(updateTableSite.getId());
                    page.setContent(content);

                    IndexingMultithread task = new IndexingMultithread(site, page);
                    task.fork();
                    tasks.add(task);

                });
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }



        return null;
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



        return null;
    }
}
