package searchengine.services.indexing;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import searchengine.model.Index;
import searchengine.model.Site;
import searchengine.model.enums.StatusEnum;
import searchengine.services.repositories.SitesRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class IndexingCheck extends Thread {

    private Site site;
    private SitesRepository sitesRepository;

    private LocalDateTime time = LocalDateTime.now();

    IndexingMultithread task;

/*    public IndexingCheck(ForkJoinPool pool, Site site, SitesRepository sitesRepository){
        this.pool = pool;
        this.site = site;
        this.sitesRepository = sitesRepository;
        run();
    }*/

    public IndexingCheck(IndexingMultithread task, Site site, SitesRepository sitesRepository){
        this.task = task;
        this.site = site;
        this.sitesRepository = sitesRepository;
        run();
    }


    @Override
    public void run() {
        try {
            for (; ; ) {
                if (task.isCompletedNormally()) {
                    site.setStatus(StatusEnum.INDEXED);
                    site.setStatusTime(time.toString());
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
/*    @Override
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
    }*/

