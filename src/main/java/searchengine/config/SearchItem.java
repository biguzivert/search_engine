package searchengine.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchItem implements Comparable<SearchItem> {

    private float rAbs;
    private float relevance;
    private String uri;
    private String title;
    private String snippet;

    public SearchItem(){}

    public SearchItem(float rAbs, String uri, String title, String snippet){
        this.rAbs = rAbs;
        this.uri = uri;
        this.title = title;
        this.snippet = snippet;
    }
    public SearchItem(float rAbs){
        this.rAbs = rAbs;
    }

    @Override
    public int compareTo(SearchItem o) {
        return (int) (this.rAbs - o.getRAbs());
    }
}
