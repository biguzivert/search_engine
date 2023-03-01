package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import searchengine.model.enums.StatusEnum;

import javax.persistence.*;

@Entity
@Table(name = "site")
@Getter
@Setter
public class SiteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private int id;

    @Enumerated
    @Column(columnDefinition = "ENUM", nullable = false)
    private StatusEnum status;

    @Column(name = "status_time", columnDefinition = "DATETIME", nullable = false)
    private String statusTime;

    @Column(name = "last_error", columnDefinition = "TEXT", nullable = false)
    private String lastError;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String url;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String name;
}
