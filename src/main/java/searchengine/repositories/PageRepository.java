package searchengine.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends CrudRepository<Page, Long> {

    void deleteSiteById(int id);

    @Modifying
    @Query("DELETE FROM Page p WHERE p.siteId = :id")
    @Transactional
    void deletePagesBySiteId(@Param("id") int id);

    Optional<Page> findPageByPath(String path);

    Optional<Page> findFirstPageByPath(String path);

    List<Page> findPagesBySiteId(int siteId);

    Page findPageById(int pageId);

}
