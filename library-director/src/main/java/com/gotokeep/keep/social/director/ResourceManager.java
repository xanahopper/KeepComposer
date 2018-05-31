package com.gotokeep.keep.social.director;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;

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
public final class ResourceManager extends FileDownloadSampleListener {
    private Context context;
    private File cacheDir = null;

    private ResourceManager(Context context) {
        this.context = context;
        FileDownloader.setup(context);
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
            if (cacheDir == null) {
                cacheDir = new File(cacheDirPath);
            }
        }
    }

    public static boolean isLocalFile(String path) {
        return path != null && (path.startsWith("/") || path.startsWith("file:/"));
    }

    public String getCacheFilePath(String resourcePath) {
        if (isLocalFile(resourcePath)) {
            return resourcePath;
        } else {
            return new File(cacheDir, getFileName(resourcePath)).getAbsolutePath();
        }
    }

    public boolean isResourceCached(String url) {
        File file = new File(getCacheFilePath(url));
        return file.exists();
    }

    public void cacheFiile(String url) {
        if (isLocalFile(url)) {
            return;
        }
        String path = getCacheFilePath(url);
        FileDownloader.getImpl().create(url)
                .setPath(path)
                .setListener(this)
                .start();
    }

    @NonNull
    public static String getFileName(String resourcePath) {
        return MD5(resourcePath) + "." + MimeTypeMap.getFileExtensionFromUrl(resourcePath);
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

    @Override
    protected void completed(BaseDownloadTask task) {
        Toast.makeText(context, task.getUrl() + "下载完毕", Toast.LENGTH_SHORT).show();
        Log.d("Director", "completed: " + task.getUrl());
    }

    @Override
    protected void error(BaseDownloadTask task, Throwable e) {
        Toast.makeText(context, task.getUrl() + "下载错误", Toast.LENGTH_SHORT).show();
        Log.e("Director", "error: " + task.getUrl(), e);
    }
}
