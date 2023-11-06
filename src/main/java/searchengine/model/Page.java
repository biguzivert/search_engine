package searchengine.model;


import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.persistence.Index;

@Entity
@Table(name = "page", indexes = {@Index(columnList = "path", name = "path_index", unique = true)})
@Getter
@Setter
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private int id;

    @Column(name = "site_id", insertable = false, updatable = false)
    private int siteId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "site_id", nullable = false, foreignKey = @ForeignKey(name = "FK_page_site"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Site site;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String path;

    @Column(nullable = false)
    private int code;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    public Page(){}

    public Page(String content, int code, String path, Site site, int siteId){
        this.content = content;
        this.code = code;
        this.path = path;
        this.site = site;
        this.siteId = siteId;
    }

}
