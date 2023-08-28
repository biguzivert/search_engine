package searchengine.model.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;

import java.util.List;

@Repository
public interface PageRepository extends CrudRepository<Page, Long> {

    void deleteSiteById(int id);

    Page findPageByPath(String path);

    List<Page> findPagesBySiteId(int siteId);

    Page findPageById(int pageId);

}
