package com.gotokeep.keep.composer.demo;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.RawRes;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.gotokeep.keep.composer.demo", appContext.getPackageName());
        Uri localUri = Uri.parse("/sdcard/DCIM/x.mp4");

        String filePath = getRawUri(appContext, R.raw.pattern_all);
        File patternFile = new File(filePath);
        try {
            InputStream is = appContext.getContentResolver().openInputStream(Uri.parse(filePath));
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(true, patternFile.exists());
    }

    public String getRawUri(Context context, @RawRes int rawId) {
        return getResourceUri(context, "raw", rawId);
    }

    private String getResourceUri(Context context, String resType, int resId) {
        return String.format(Locale.getDefault(), "%s://%s/%d",
                ContentResolver.SCHEME_ANDROID_RESOURCE,
                context.getPackageName(), resId);
    }
}
