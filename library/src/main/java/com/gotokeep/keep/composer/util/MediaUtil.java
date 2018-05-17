package com.gotokeep.keep.composer.util;

import android.media.MediaCodec;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/13 14:40
 */
public class MediaUtil {
    public static int getRotation(String filePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        int rotation = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
        retriever.release();
        return rotation;
    }

    public static ByteBuffer getInputBuffer(MediaCodec codec, int inputIndex) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return codec.getInputBuffer(inputIndex);
        } else {
            return codec.getInputBuffers()[inputIndex];
        }
    }

    public static ByteBuffer getOutputBuffer(MediaCodec codec, int inputIndex) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return codec.getOutputBuffer(inputIndex);
        } else {
            return codec.getOutputBuffers()[inputIndex];
        }
    }

    public static String getName(String filePath) {
        String name = Uri.parse(filePath).getLastPathSegment();
        return name != null ? name : UUID.randomUUID().toString().substring(0, 5);
    }

    public static float clamp(float value, float low, float high) {
        if (value < low) {
            return low;
        } else if (value > high) {
            return high;
        } else {
            return value;
        }
    }
}
