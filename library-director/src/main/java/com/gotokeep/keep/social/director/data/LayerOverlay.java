package com.gotokeep.keep.social.director.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 11:39
 */
public final class LayerOverlay implements MediaData {
    private String id;
    private String url;

    public LayerOverlay(String id, String url) {
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
        return "LayerOverlay{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    @Override
    public List<String> getResources() {
        List<String> result = new ArrayList<>();
        result.add(url);
        return result;
    }
}
