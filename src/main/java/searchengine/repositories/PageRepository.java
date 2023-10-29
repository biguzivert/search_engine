package searchengine.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends CrudRepository<Page, Long> {

    void deleteSiteById(int id);

    Optional<Page> findPageByPath(String path);

    Optional<Page> findFirstPageByPath(String path);

    List<Page> findPagesBySiteId(int siteId);

    Page findPageById(int pageId);

}
