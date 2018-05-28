package com.gotokeep.keep.director.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 11:40
 */
public final class WatermarkOverlay implements MediaData {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "WatermarkOverlay{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public List<String> getResources() {
        return new ArrayList<>();
    }
}
