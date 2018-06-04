package com.gotokeep.keep.data.model.director;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 11:38
 */
public final class Music implements MediaData {
    private String id;
    private String url;

    public Music(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Music{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    @Override
    public List<String> getResources() {
        return new ArrayList<>();
    }
}
