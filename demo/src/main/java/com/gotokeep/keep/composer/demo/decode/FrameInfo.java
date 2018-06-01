package com.gotokeep.keep.composer.demo.decode;

import com.gotokeep.keep.social.composer.util.TimeUtil;

public class FrameInfo {
    public long frame;
    public long presentationTimeUs;
    public boolean decoded;
    public int sourceIndex;
    public TimeRange inputBuffer = new TimeRange();
    public TimeRange readSample = new TimeRange();
    public TimeRange decode = new TimeRange();
    public TimeRange updateTexImage = new TimeRange();

    @Override
    public String toString() {
        return "FrameInfo{\n " +
                "presentationTimeUs=" + TimeUtil.usToString(presentationTimeUs) +
                ",\n decoded=" + decoded +
                ",\n sourceIndex=" + sourceIndex +
                ",\n inputBuffer=" + inputBuffer +
                ",\n readSample=" + readSample +
                ",\n decode=" + decode +
                ",\n updateTexImage=" + updateTexImage +
                '}';
    }
}