package com.gotokeep.keep.composer.demo.decode;

import com.gotokeep.keep.social.composer.util.TimeUtil;

public class TimeRange {
    public long startTime;
    public long endTime;

    @Override
    public String toString() {
        return "TimeRange(" + TimeUtil.nsToUs(endTime - startTime) + ")";
    }

    public long duration() {
        return endTime - startTime;
    }
}