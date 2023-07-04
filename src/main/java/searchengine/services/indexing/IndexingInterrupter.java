package searchengine.services.indexing;

import searchengine.model.Site;
import searchengine.model.enums.StatusEnum;
import searchengine.services.repositories.SitesRepository;

import java.util.List;

public class IndexingInterrupter extends Thread{

    private Thread indexingThread;
    private SitesRepository sitesRepository;

    private final String ERROR_MESSAGE = "Индексация остановлена пользователем";

    public IndexingInterrupter (Thread indexingThread){
        this.indexingThread = indexingThread;
    }

    @Override
    public void run() {
        indexingThread.interrupt();
        Iterable<Site> sites = sitesRepository.findAll();
        for(Site s : sites){
            if(s.getStatus() != StatusEnum.INDEXED){
                s.setStatus(StatusEnum.FAILED);
                s.setLastError(ERROR_MESSAGE);
            }
        }
    }
}
