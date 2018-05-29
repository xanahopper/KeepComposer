package com.gotokeep.keep.composer;

import com.gotokeep.keep.composer.util.TimeUtil;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/29 21:41
 */
public class TimeUtilTest {
    @Test
    public void findKeyFrameTest() {
        List<Long> keyFrames = new ArrayList<>();
        keyFrames.add(0L);
        assertEquals(0L, TimeUtil.findClosesKeyFrame(keyFrames, 0L));
        assertEquals(0L, TimeUtil.findClosesKeyFrame(keyFrames, 10000L));
        keyFrames.add(30000L);
        assertEquals(0L, TimeUtil.findClosesKeyFrame(keyFrames, 0L));
        assertEquals(0L, TimeUtil.findClosesKeyFrame(keyFrames, 10000L));
        assertEquals(30000L, TimeUtil.findClosesKeyFrame(keyFrames, 30000L));
        assertEquals(30000L, TimeUtil.findClosesKeyFrame(keyFrames, 40000L));
        keyFrames.add(60000L);
        assertEquals(0L, TimeUtil.findClosesKeyFrame(keyFrames, 0L));
        assertEquals(0L, TimeUtil.findClosesKeyFrame(keyFrames, 10000L));
        assertEquals(30000L, TimeUtil.findClosesKeyFrame(keyFrames, 30000L));
        assertEquals(30000L, TimeUtil.findClosesKeyFrame(keyFrames, 40000L));
        assertEquals(60000L, TimeUtil.findClosesKeyFrame(keyFrames, 60000L));
        assertEquals(60000L, TimeUtil.findClosesKeyFrame(keyFrames, 70000L));
        keyFrames.add(90000L);
        assertEquals(0L, TimeUtil.findClosesKeyFrame(keyFrames, 0L));
        assertEquals(0L, TimeUtil.findClosesKeyFrame(keyFrames, 10000L));
        assertEquals(30000L, TimeUtil.findClosesKeyFrame(keyFrames, 30000L));
        assertEquals(30000L, TimeUtil.findClosesKeyFrame(keyFrames, 40000L));
        assertEquals(60000L, TimeUtil.findClosesKeyFrame(keyFrames, 60000L));
        assertEquals(60000L, TimeUtil.findClosesKeyFrame(keyFrames, 70000L));
        assertEquals(90000L, TimeUtil.findClosesKeyFrame(keyFrames, 90000L));
        assertEquals(90000L, TimeUtil.findClosesKeyFrame(keyFrames, 100000L));
    }
}
