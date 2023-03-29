package searchengine.model;


import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import searchengine.config.Site;

import javax.persistence.*;

@Entity
@Table(name = "page", indexes = @Index(columnList = "path"))
@Getter
@Setter
public class PageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private int id;

    @Column(name = "site_id", insertable = false, updatable = false)
    private int siteId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "site_id", nullable = false, foreignKey = @ForeignKey(name = "FK_page_site"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private SiteEntity site;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String path;

    @Column(nullable = false)
    private int code;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

}
