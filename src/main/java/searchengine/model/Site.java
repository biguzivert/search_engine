package searchengine.model;

import com.sun.istack.NotNull;

import javax.persistence.*;

@Entity
@Table(name = "site")
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @NotNull
    private int id;

    @Enumerated
    @NotNull
    @Column(columnDefinition = "ENUM")
    private StatusEnum status;

    @NotNull
    @Column(name = "status_time", columnDefinition = "DATETIME")
    private String statusTime;

    @NotNull
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(columnDefinition = "VARCHAR(255)")
    @NotNull
    private String url;

    @Column(columnDefinition = "VARCHAR(255)")
    @NotNull
    private String name;
}
