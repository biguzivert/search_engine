package searchengine.services.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.enums.StatusEnum;

@Repository
public interface SitesRepository extends CrudRepository<Site, Long> {

    Site findSiteByUrl(String url);

    @Modifying
    @Query("update Site s set s.status = :newStatus, s.lastError = :error where s.status = :oldStatus")
    void updateStatusAndError(@Param("oldStatus")StatusEnum oldStatus, @Param("newStatus")StatusEnum newStatus, @Param("error") String error);
}
