package com.gotokeep.keep.social.composer.decode;

import android.media.MediaCodec;
import android.support.v4.util.Pools;

/**
 * MediaCodec 复用池, 假定所有 mime 类型相同
 *
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-01 16:47
 */
public final class MediaCodecPool extends Pools.SynchronizedPool<MediaCodec> {
    private static final int MAX_POOL_SIZE = 8;

    /**
     * Creates a new instance.
     *
     * @throws IllegalArgumentException If the max pool size is less than zero.
     */
    public MediaCodecPool() {
        super(MAX_POOL_SIZE);
    }
}
