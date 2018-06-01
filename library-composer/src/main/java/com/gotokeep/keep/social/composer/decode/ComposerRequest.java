package com.gotokeep.keep.social.composer.decode;

import android.os.SystemClock;
import android.support.v4.util.Pools;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-01 18:29
 */
public class ComposerRequest {
    private static Pools.Pool<ComposerRequest> pool = new Pools.SynchronizedPool<>(32);

    public long requestTimeMs;
    public long requestDecodeTimeUs;

    public static ComposerRequest obtain() {
        ComposerRequest request = pool.acquire();
        if (request == null) {
            request = new ComposerRequest();
        }
        request.requestTimeMs = SystemClock.elapsedRealtime();
        return request;
    }

    public static ComposerRequest obtain(long requestDecodeTimeUs) {
        ComposerRequest request = obtain();
        request.requestDecodeTimeUs = requestDecodeTimeUs;
        return request;
    }

    public void recycle() {
        requestTimeMs = 0;
        requestDecodeTimeUs = 0;
        pool.release(this);
    }
}
