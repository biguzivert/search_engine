package searchengine.services.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.Site;

@Repository
public interface PageRepository extends CrudRepository<Page, Long> {

    void deleteSiteById(int id);

    Page findPageByPath(String path);

}
