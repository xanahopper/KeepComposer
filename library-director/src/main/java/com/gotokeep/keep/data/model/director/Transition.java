package com.gotokeep.keep.data.model.director;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 11:45
 */
public final class Transition implements MediaData {
    private String name;
    private long duration;
    public static final int DEFAULT_DURATION = 400;

    public Transition(String name, long duration) {
        this.name = name;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "Transition{" +
                "name='" + name + '\'' +
                ", duration=" + duration +
                '}';
    }

    @Override
    public List<String> getResources() {
        return new ArrayList<>();
    }

    public static boolean isAvailable(Transition transition) {
        return transition != null && !TextUtils.isEmpty(transition.getName());
    }
}
