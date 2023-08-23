package searchengine.services.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.repositories.LemmaRepository;
import searchengine.model.repositories.PageRepository;
import searchengine.model.repositories.SitesRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final Random random = new Random();
    private final SitesList sites;

    private SitesRepository sitesRepository;

    private PageRepository pageRepository;

    private LemmaRepository lemmaRepository;
    private SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    @Autowired
    public StatisticsServiceImpl(SitesRepository sitesRepository, PageRepository pageRepository, LemmaRepository lemmaRepository,SitesList sitesList){
        this.sitesRepository = sitesRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.sites = sitesList;
    }

    @Override
    public StatisticsResponse getStatistics() {
        String[] statuses = { "INDEXED", "FAILED", "INDEXING" };
        String[] errors = {
                "Ошибка индексации: главная страница сайта не доступна",
                "Ошибка индексации: сайт не доступен",
                ""
        };

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for(int i = 0; i < sitesList.size(); i++) {
            Site site = sitesList.get(i);
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            searchengine.model.Site siteDB = sitesRepository.findSiteByUrl(site.getUrl());
            List<Page> pagesOnSite = pageRepository.findPagesBySiteId(siteDB.getId());
            int pages = pagesOnSite.size();
            List<Lemma> lemmasOnSite = lemmaRepository.findLemmasBySiteId(siteDB.getId());
            int lemmas = lemmasOnSite.size();
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(siteDB.getStatus());
            item.setError(siteDB.getLastError());
            //???
            try{
                String statusTimeString = siteDB.getStatusTime();
                Date statusTimeDateFormat = format.parse(statusTimeString);
                long statusTime = statusTimeDateFormat.getTime()/1000;
                item.setStatusTime(statusTime);
            } catch (ParseException ex){
                ex.printStackTrace();
            }
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
