package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.Site;
import searchengine.model.enums.StatusEnum;
import searchengine.services.repositories.PageRepository;
import searchengine.services.repositories.SitesRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class IndexingMultithread extends RecursiveTask<Site> {
    private searchengine.config.Site site;
    private SitesRepository sitesRepository;
    private PageRepository pageRepository;

    public IndexingMultithread(searchengine.config.Site site) {
        this.site = site;
    }
    @Override
    protected Site compute() {

        List<IndexingMultithread> taskList = new ArrayList<>();
        sites.forEach(s -> {

            searchengine.model.Site site = sitesRepository.findSiteByUrl(s.getUrl());
            sitesRepository.deleteById(site);
            //удаление Site из таблицы Page реализовать

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



        return null;
    }
}
