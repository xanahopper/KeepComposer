package com.gotokeep.keep.composer.timeline.item;

import com.gotokeep.keep.composer.timeline.MediaItem;

import java.util.Map;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 12:10
 */
public class FilterItem extends MediaItem {
    protected String name;
    protected Map<String, Object> params;

    public FilterItem(String name, Map<String, Object> params) {
        super(TYPE_DRAW, 0, null);
        this.params = params;
        this.name = name;
    }

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
}
