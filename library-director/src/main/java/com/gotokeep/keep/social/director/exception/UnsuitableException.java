package com.gotokeep.keep.social.director.exception;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 14:55
 */
public class UnsuitableException extends Exception {
    private int minFragment;
    private int maxFragment;
    public UnsuitableException(String message, int minFragment, int maxFragment) {
        super(message);
        this.minFragment = minFragment;
        this.maxFragment = maxFragment;
    }

    public int getMinFragment() {
        return minFragment;
    }

    public int getMaxFragment() {
        return maxFragment;
    }
}
