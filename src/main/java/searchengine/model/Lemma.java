package searchengine.model;

import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.lang.Nullable;

import javax.persistence.*;

@Entity
@Table (name = "lemma")
@Getter
@Setter
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private int id;

    @Column(name = "site_id", nullable = false)
    private int siteId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "site_id", nullable = false, foreignKey = @ForeignKey(name = "FK_lemma_site"), insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Site site;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String lemma;

    @NotNull
    private int frequency;
}
