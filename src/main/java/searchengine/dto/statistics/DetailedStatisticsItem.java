package searchengine.dto.statistics;

import lombok.Data;
import searchengine.model.enums.StatusEnum;

@Data
public class DetailedStatisticsItem {
    private String url;
    private String name;
    private StatusEnum status;
    private long statusTime;
    private String error;
    private int pages;
    private int lemmas;
}
