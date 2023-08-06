package searchengine.services.indexing;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import searchengine.model.Index;
import searchengine.model.Site;
import searchengine.model.enums.StatusEnum;
import searchengine.services.repositories.SitesRepository;

import java.time.LocalDateTime;
import java.util.concurrent.ForkJoinPool;

public class IndexingCheck extends Thread{

    private ForkJoinPool pool;
    private Site site;
    private SitesRepository sitesRepository;
    private LocalDateTime time = LocalDateTime.now();
    public IndexingCheck(ForkJoinPool pool, Site site, SitesRepository sitesRepository){
        this.pool = pool;
        this.site = site;
        this.sitesRepository = sitesRepository;
        run();
    }

    @Override
    public void run() {
        try {
            for (; ; ) {
                if (!pool.isShutdown() && pool.getActiveThreadCount() == 0) {
                    site.setStatus(StatusEnum.INDEXED);
                    site.setStatusTime(time.toString());
                    sitesRepository.save(site);
                    break;

                } else {
                    Thread.sleep(1000);
                }
            }
        } catch (InterruptedException ex){
            ex.printStackTrace();
        }
    }
}
