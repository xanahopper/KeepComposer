package com.gotokeep.keep.director;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class KeepDirectorTest {
    @Test
    public void setScriptTextTest() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        AssetManager manager = appContext.getAssets();
        String scriptText = "";
        try {
            InputStream is = manager.open("patter_all_template.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                sb.append(line);
                sb.append('\n');
                line = reader.readLine();
            }
            reader.close();
            scriptText = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        KeepDirector director = new KeepDirector();
        director.setScriptText(scriptText);
        Log.d("Test",  director.getScript().toString());
    }
}
