package com.gotokeep.keep.director.util;

import android.text.TextUtils;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-28 10:43
 */
public final class FileUtil {
    public static String getFileName(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }
        int index = filePath.lastIndexOf("/");
        if (index == -1) {
            return filePath;
        }
        return filePath.substring(index + 1);
    }
}
