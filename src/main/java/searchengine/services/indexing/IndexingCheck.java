package searchengine.services.indexing;

import searchengine.model.Site;
import searchengine.model.enums.StatusEnum;
import searchengine.repositories.SitesRepository;

import java.time.LocalDateTime;
import java.util.concurrent.*;

public class IndexingCheck implements Callable<Boolean> {

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
    }


    @Override
    public Boolean call() {
        boolean isIndexed = false;
        try {
            for (; ; ) {
                if (task.isCompletedNormally()) {
                    isIndexed = true;
                    site.setStatus(StatusEnum.INDEXED);
                    site.setStatusTime(time.toString());
                    break;
                } else if (task.isCompletedAbnormally()) {
                    isIndexed = false;
                    site.setStatus(StatusEnum.FAILED);
                    site.setStatusTime(time.toString());
                    site.setLastError("Индексирование прекращено");
                } else {
                    Thread.sleep(1000);
                }
            }
        } catch (InterruptedException ex){
            ex.printStackTrace();
        }
        return isIndexed;
     }
}


