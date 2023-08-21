package searchengine.model.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.List;

@Repository
public interface IndexRepository extends CrudRepository<Index, Long> {

    List<Page> findPagesByLemmaId(int lemmaId);

    @Modifying
    @Query("SELECT rank FROM Lemma l WHERE l.page_id = :pageId, l.lemma_id = :lemmaId")
    float findRankByLemmaIdOnPage(@Param("lemmaId") int lemmaId, @Param("pageId") int pageId);
}
