package searchengine.model;

import com.sun.istack.NotNull;

import javax.persistence.*;

@Entity
@Table(name = "page")
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @NotNull
    private int id;

    @Column(name = "site_id")
    @NotNull
    private int siteId;

    @Column(columnDefinition = "TEXT")
    @NotNull
    private String path;

    @NotNull
    private int code;

    @Column(columnDefinition = "MEDIUMTEXT")
    @NotNull
    private String content;
}
