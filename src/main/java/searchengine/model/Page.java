package searchengine.model;


import javax.persistence.*;

@Entity
@Table(name = "page", indexes = @Index(columnList = "path"))
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private int id;

    @ManyToOne(targetEntity = Site.class)
    @JoinColumn(name = "id")
    @Column(name = "site_id", nullable = false)
    private int siteId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String path;

    @Column(nullable = false)
    private int code;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;
}
