package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import searchengine.model.enums.StatusEnum;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "site")
@Getter
@Setter
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private int id;

    @Enumerated(EnumType.STRING)
  //  @Column(columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')", nullable = false)
    @Column(nullable = false)
    private StatusEnum status;

    @Column(name = "status_time", columnDefinition = "DATETIME", nullable = false)
    private String statusTime;

    @Column(name = "last_error", columnDefinition = "TEXT", nullable = false)
    private String lastError;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String url;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String name;

    @OneToMany(mappedBy = "site", fetch = FetchType.EAGER)
    private Set<Page> pages = new HashSet<>();

    public Site(String name, StatusEnum status, String url, String statusTime){
        this.name = name;
        this.status = status;
        this.url = url;
        this.statusTime = statusTime;
    }

    public Site(){

    }
}
