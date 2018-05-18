package com.gotokeep.keep.director.data;

import java.util.Map;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 11:46
 */
public final class DefaultConfig {
    private Transition transition;
    private Map<String, Float> playSpeed;

    public Transition getTransition() {
        return transition;
    }

    public void setTransition(Transition transition) {
        this.transition = transition;
    }

    public Map<String, Float> getPlaySpeed() {
        return playSpeed;
    }

    public void setPlaySpeed(Map<String, Float> playSpeed) {
        this.playSpeed = playSpeed;
    }

    @Override
    public String toString() {
        return "DefaultConfig{" +
                "transition=" + transition +
                ", playSpeed=" + playSpeed +
                '}';
    }
}
