package searchengine.model.repositories;


import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;

import java.util.List;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma, Long> {

    Lemma findLemmaByLemma(String lemma);

    @Modifying
    @Query ("UPDATE Lemma l SET l.frequency = l.frequency + :count WHERE l.lemma = :lemma")
    int increaseFrequency(@Param("lemma") String lemma, @Param("count") int increaseBy);

    List<Lemma> findLemmasBySiteId(int siteId);
}