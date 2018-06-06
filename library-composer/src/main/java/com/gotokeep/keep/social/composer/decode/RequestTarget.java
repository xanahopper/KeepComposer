package com.gotokeep.keep.social.composer.decode;

import android.support.annotation.NonNull;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-01 18:46
 */
public interface RequestTarget {
    void sendRequest(@NonNull ComposerRequest request);
}
