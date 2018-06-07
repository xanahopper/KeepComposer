package com.gotokeep.keep.data.model.director;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 11:45
 */
public final class Filter implements MediaData {
    private String name;
    private Map<String, Object> params;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "Filter{" +
                "name='" + name + '\'' +
                ", params=" + params +
                '}';
    }

    @Override
    public List<String> getResources() {
        return new ArrayList<>();
    }

    public static boolean isAvailable(Filter filter) {
        return filter != null && !TextUtils.isEmpty(filter.name);
    }
}
