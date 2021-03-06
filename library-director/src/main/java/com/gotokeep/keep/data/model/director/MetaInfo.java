package com.gotokeep.keep.data.model.director;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 11:38
 */
public final class MetaInfo {
    private String pattern;
    private long duration;
    private int minFragment;
    private int maxFragment;
    private Filter filter;
    private Resource title;
    private String music;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getMinFragment() {
        return minFragment;
    }

    public void setMinFragment(int minFragment) {
        this.minFragment = minFragment;
    }

    public int getMaxFragment() {
        return maxFragment;
    }

    public void setMaxFragment(int maxFragment) {
        this.maxFragment = maxFragment;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public String getMusic() {
        return music;
    }

    public void setMusic(String music) {
        this.music = music;
    }

    public Resource getTitle() {
        return title;
    }

    public void setTitle(Resource title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "MetaInfo{" +
                "pattern='" + pattern + '\'' +
                ", duration=" + duration +
                ", minFragment=" + minFragment +
                ", maxFragment=" + maxFragment +
                ", filter=" + filter +
                ", music=" + music +
                '}';
    }
}
