package com.gotokeep.keep.social.composer.source;

import android.support.v4.util.Pools;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-15 20:24
 */
public final class SampleDataInfo {
    private static final int PoolSize = 100;
    private static Pools.SynchronizedPool<SampleDataInfo> cachePool = new Pools.SynchronizedPool<>(100);

    public static SampleDataInfo obtain() {
        SampleDataInfo info = cachePool.acquire();
        if (info == null) {
            info = new SampleDataInfo();
        }
        return info;
    }

    public static void release(SampleDataInfo info) {
        cachePool.release(info);
    }

    public static void cleanUp() {
        cachePool = null;
    }

    public int bufferSize;
    public long presentationTimeUs;
    public int flags;
    public boolean ended;

    private SampleDataInfo() {
    }

    public SampleDataInfo(int bufferSize, long presentationTimeUs, int flags, boolean ended) {
        this.bufferSize = bufferSize;
        this.presentationTimeUs = presentationTimeUs;
        this.flags = flags;
        this.ended = ended;
    }

    @Override
    public String toString() {
        return "SampleDataInfo{" +
                "bufferSize=" + bufferSize +
                ", presentationTimeUs=" + presentationTimeUs +
                ", flags=" + flags +
                ", ended=" + ended +
                '}';
    }
}
