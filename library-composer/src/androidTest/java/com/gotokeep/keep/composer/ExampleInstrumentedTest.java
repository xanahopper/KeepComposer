package com.gotokeep.keep.composer;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
//    @Test
//    public void useAppContext() {
//        // Context of the app under test.
//        Context appContext = InstrumentationRegistry.getTargetContext();
//
//        assertEquals("com.gotokeep.keep.composer.test", appContext.getPackageName());
//    }

    @Test
    public void rectRotationBoundTest() {
        RectF rect = new RectF(0, 0, 10, 10);
        Matrix matrix = new Matrix();
        matrix.postRotate(30, 5, 5);
        matrix.mapRect(rect);
        Log.d("Test", rect.toString());
        assertEquals(1, 1);
    }
}
