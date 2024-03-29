package searchengine.utils.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Page;
import searchengine.model.enums.StatusEnum;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SitesRepository;
import searchengine.services.statistics.StatisticsService;
import searchengine.utils.lemmatization.Lemmatization;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final Random random = new Random();
    private final SitesList sites;

    private SitesRepository sitesRepository;

    private PageRepository pageRepository;

    private LemmaRepository lemmaRepository;
    private IndexRepository indexRepository;
    private SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    @Autowired
    public StatisticsServiceImpl(SitesRepository sitesRepository, PageRepository pageRepository, LemmaRepository lemmaRepository,SitesList sitesList, IndexRepository indexRepository){
        this.sitesRepository = sitesRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.sites = sitesList;
    }

    @Override
    public StatisticsResponse getStatistics() {
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        List<searchengine.model.Site> sitesInDB = sitesRepository.findAll();
        if(sitesInDB.isEmpty()){
            total.setIndexing(false);
            total.setLemmas(0);
            total.setPages(0);
            total.setSites(0);
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setError("");
            item.setLemmas(0);
            item.setPages(0);
            item.setStatus(StatusEnum.INDEXING);
            item.setName("");
            item.setUrl("");
            detailed.add(item);
            data.setDetailed(detailed);
            data.setTotal(total);
            response.setStatistics(data);
            response.setResult(true);
            return response;
        }
        for(int i = 0; i < sitesList.size(); i++) {
            Site site = sitesList.get(i);
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());

            searchengine.model.Site siteDB = sitesRepository.findSiteByUrl(site.getUrl());
            if(siteDB == null){
                continue;
            }

            Lemmatization lemmatization = new Lemmatization(siteDB, lemmaRepository, indexRepository, pageRepository){};
            lemmatization.lemmatizationIndexing();
            List<Page> pagesOnSite = pageRepository.findPagesBySiteId(siteDB.getId());
            int pages = pagesOnSite.size();
            List<String> lemmasOnSite = lemmaRepository.findLemmasBySiteId(siteDB.getId());
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


        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
