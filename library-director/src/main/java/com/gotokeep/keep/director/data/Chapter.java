package com.gotokeep.keep.director.data;

import java.util.List;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 11:47
 */
public final class Chapter implements MediaData {
    public static final String TYPE_CHAPTER = "chapter";
    public static final String TYPE_FOOTAGE = "footage";
    public static final String TYPE_GENERATE = "generate";

    private String type;
    private String source;  // only used in type footage;
    private long duration;
    private float playSpeed = 1f;
    private Filter filter;
    private Transition transition;
    private List<Overlay> overlay;
    private List<String> tag;
    private List<Effect> effect;
    private String music;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public float getPlaySpeed() {
        return playSpeed;
    }

    public void setPlaySpeed(float playSpeed) {
        this.playSpeed = playSpeed;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Transition getTransition() {
        return transition;
    }

    public void setTransition(Transition transition) {
        this.transition = transition;
    }

    public List<Overlay> getOverlay() {
        return overlay;
    }

    public void setOverlay(List<Overlay> overlay) {
        this.overlay = overlay;
    }

    public List<String> getTag() {
        return tag;
    }

    public void setTag(List<String> tag) {
        this.tag = tag;
    }

    public List<Effect> getEffect() {
        return effect;
    }

    public void setEffect(List<Effect> effect) {
        this.effect = effect;
    }

    public String getMusic() {
        return music;
    }

    public void setMusic(String music) {
        this.music = music;
    }

    @Override
    public String toString() {
        return "Chapter{" +
                "type='" + type + '\'' +
                ", source='" + source + '\'' +
                ", duration=" + duration +
                ", playSpeed=" + playSpeed +
                ", filter=" + filter +
                ", transition=" + transition +
                ", overlay=" + overlay +
                ", tag=" + tag +
                ", effect=" + effect +
                ", music=" + music +
                '}';
    }
}
