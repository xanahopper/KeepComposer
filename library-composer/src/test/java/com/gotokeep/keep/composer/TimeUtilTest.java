package com.gotokeep.keep.composer;


import com.gotokeep.keep.social.composer.util.TimeUtil;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/6/3 11:20
 */
public class TimeUtilTest {
    @Test
    public void findCloseestKeyFrameTest() {
        List<Long> keyFrames = new ArrayList<>();
        keyFrames.add(0L);
        keyFrames.add(3000L);
        keyFrames.add(6000L);
        keyFrames.add(9000L);
        keyFrames.add(9500L);
        assertEquals(0L, TimeUtil.findClosestKeyFrame(keyFrames, 0L));
        assertEquals(0L, TimeUtil.findClosestKeyFrame(keyFrames, 2000L));
        assertEquals(3000L, TimeUtil.findClosestKeyFrame(keyFrames, 3000L));
        assertEquals(3000L, TimeUtil.findClosestKeyFrame(keyFrames, 4000L));
        assertEquals(6000L, TimeUtil.findClosestKeyFrame(keyFrames, 6000L));
        assertEquals(9000L, TimeUtil.findClosestKeyFrame(keyFrames, 9000L));
        assertEquals(9500L, TimeUtil.findClosestKeyFrame(keyFrames, 10000L));
    }
}
