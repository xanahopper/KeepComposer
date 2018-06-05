package com.gotokeep.keep.data.model.director;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 11:52
 */
public final class ChapterSet {
    @SerializedName("default")
    private DefaultConfig defaultConfig;
    private List<Chapter> data;

    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(DefaultConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    public List<Chapter> getData() {
        return data;
    }

    public void setData(List<Chapter> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ChapterSet{" +
                "defaultConfig=" + defaultConfig +
                ", data=" + data +
                '}';
    }
}
