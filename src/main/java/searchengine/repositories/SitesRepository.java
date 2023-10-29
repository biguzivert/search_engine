package searchengine.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Site;
import searchengine.model.enums.StatusEnum;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface SitesRepository extends CrudRepository<Site, Long> {

    Site findSiteByUrl(String url);

    List<Site> findAllSitesByUrl(String url);
    List<Site> findAll();

    @Modifying
    @Query("update Site s set s.status = :newStatus, s.lastError = :error where s.status = :oldStatus")
    void updateStatusAndError(@Param("oldStatus")StatusEnum oldStatus, @Param("newStatus")StatusEnum newStatus, @Param("error") String error);

    @Modifying
    @Query("delete from Site s where s.url = :url")
    @Transactional
    void deleteAllSitesByUrl(@Param("url") String url);
}
