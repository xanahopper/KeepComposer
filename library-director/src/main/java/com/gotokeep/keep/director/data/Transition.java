package com.gotokeep.keep.director.data;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 11:45
 */
public final class Transition implements MediaData {
    private String name;
    private long duration;

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
}
