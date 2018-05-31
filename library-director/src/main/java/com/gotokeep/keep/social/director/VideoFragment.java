package com.gotokeep.keep.social.director;

import java.util.List;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 14:19
 */
public final class VideoFragment {
    private String file;
    private int step;
    private List<String> tag;
    private long durationMs;

    public VideoFragment(String file, int step, List<String> tag) {
        this.file = file;
        this.step = step;
        this.tag = tag;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public List<String> getTag() {
        return tag;
    }

    public void setTag(List<String> tag) {
        this.tag = tag;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }
}
