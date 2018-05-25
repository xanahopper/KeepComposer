package com.gotokeep.keep.director;

import android.content.Context;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 下载、缓存与替换本地文件的资源管理器
 *
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-25 18:15
 */
public final class ResourceManager {
    private Context context;
    private File cacheDir = null;

    private ResourceManager(Context context) {
        this.context = context;
    }

    private static final class InstanceHolder {
        static ResourceManager instance;
    }

    public static ResourceManager getInstance(Context context) {
        synchronized (InstanceHolder.class) {
            if (InstanceHolder.instance == null) {
                InstanceHolder.instance = new ResourceManager(context);
            }
        }
        return InstanceHolder.instance;
    }

    public void init(String cacheDirPath) {
        synchronized (this) {
            if (cacheDir != null) {
                cacheDir = new File(cacheDirPath);
            }
        }
    }

    public static boolean isLocalFile(String path) {
        return path != null && (path.startsWith("/") || path.startsWith("file:/"));
    }

    public File getCacheFilePath(String resourcePath) {
        if (isLocalFile(resourcePath)) {
            return new File(resourcePath);
        } else {
            return new File(cacheDir, MD5(resourcePath) + MimeTypeMap.getFileExtensionFromUrl(resourcePath));
        }
    }



    public static String MD5(String sourceStr) {
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sourceStr.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            result = buf.toString();
        } catch (NoSuchAlgorithmException e) {
//            System.out.println(e);
        }
        return result;
    }

}
