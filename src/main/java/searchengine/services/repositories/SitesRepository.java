package searchengine.services.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.Site;

@Repository
public interface SitesRepository extends CrudRepository<Site, Long> {

    Site findSiteByUrl(String url);


}
