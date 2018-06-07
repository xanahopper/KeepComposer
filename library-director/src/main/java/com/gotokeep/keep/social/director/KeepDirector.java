package com.gotokeep.keep.social.director;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.gotokeep.keep.data.model.director.Chapter;
import com.gotokeep.keep.data.model.director.ChapterSet;
import com.gotokeep.keep.data.model.director.DirectorScript;
import com.gotokeep.keep.data.model.director.MetaInfo;
import com.gotokeep.keep.social.composer.timeline.SourceTimeline;
import com.gotokeep.keep.social.composer.timeline.Timeline;
import com.gotokeep.keep.social.composer.timeline.Track;
import com.gotokeep.keep.social.director.exception.UnsuitableException;
import com.gotokeep.keep.social.director.pattern.BasePattern;
import com.gotokeep.keep.social.director.pattern.PatternAll;
import com.gotokeep.keep.social.director.pattern.PatternLoop;
import com.gotokeep.keep.social.director.pattern.PatternRandom;
import com.gotokeep.keep.social.director.pattern.PatternSequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 11:36
 */
public final class KeepDirector implements ResourceManager.ResourceListener {
    public static final String PATTERN_ALL = "all";
    public static final String PATTERN_ALL_ODD = "allOdd";
    public static final String PATTERN_ALL_EVEN = "allEven";
    public static final String PATTERN_RANOM = "random";
    public static final String PATTERN_FIRST = "first";
    public static final String PATTERN_LAST = "last";
    public static final String PATTERN_SEQUENCE = "sequence";
    public static final String PATTERN_LOOP = "loop";

    public static final int LAYER_SOURCE = 0;
    public static final int LAYER_TRANSITION = 1;
    public static final int LAYER_FILTER = 2;
    public static final int LAYER_OVERLAY = 3;

    public static final int DEFAULT_TRACK_COUNT = 4;

    private ResourceManager resourceManager;
    private DirectorScript script;
    private Gson gson;
    private EventListener listener;
    private List<String> resourceList = new ArrayList<>();
    private Map<String, Boolean> resourceVerification = new HashMap<>();
    private Handler eventHandler;

    public KeepDirector(Context context) {
        gson = new Gson();
        resourceManager = ResourceManager.getInstance(context);
        resourceManager.addResourceListener(this);
        SelectPatternFactory.setup(context);
        eventHandler = new Handler(Looper.getMainLooper());
    }

    public KeepDirector(Context context, EventListener listener) {
        this(context);
        setListener(listener);
    }

    public KeepDirector(Context context, String scriptText) {
        this(context);
        setScriptText(scriptText);
    }

    public KeepDirector(Context context, String scriptText, EventListener listener) {
        this(context);
        setListener(listener);
        setScriptText(scriptText);
    }

    public void release() {
        resourceManager.removeResourceListener(this);
        SelectPatternFactory.release();
    }

    public void setScriptText(String scriptText) {
        this.script = gson.fromJson(scriptText, DirectorScript.class);
    }

    public DirectorScript getScript() {
        return script;
    }

    public void setListener(EventListener listener) {
        this.listener = listener;
    }

    /**
     * 验证并准备此脚本（即下载脚本中所有资源）
     *
     * @return 是否已经准备就绪
     */
    public boolean verifyScript() {
        if (script == null || script.getMeta() == null) {
            return false;
        }
        resourceList.clear();
        resourceVerification.clear();

        if (!TextUtils.isEmpty(script.getCover())) {
            resourceList.add(script.getCover());
        }
        MetaInfo metaInfo = script.getMeta();
        resourceList.addAll(metaInfo.getFilter().getResources());
        if (!TextUtils.isEmpty(metaInfo.getMusic())) {
            resourceList.add(metaInfo.getMusic());
        }
        if (script.getFooter() != null) {
            resourceList.addAll(script.getFooter().getResources());
        }
        if (script.getChapter() != null) {
            ChapterSet chapterSet = script.getChapter();
            if (chapterSet.getData() != null) {
                for (Chapter chapter : chapterSet.getData()) {
                    resourceList.addAll(chapter.getResources());
                }
            }
        }
        boolean verified = true;
        for (String resource : resourceList) {
            if (!TextUtils.isEmpty(resource) && !resourceManager.isResourceCached(resource)) {
                verified = false;
                resourceManager.cacheFile(resource);
                resourceVerification.put(resource, null);
            } else {
                resourceVerification.put(resource, true);
            }
        }
        return verified;
    }

    public boolean isScriptSuitable(List<VideoFragment> videoSources) {
        if (script == null) {
            return false;
        }
        MetaInfo metaInfo = script.getMeta();
        return videoSources.size() >= metaInfo.getMinFragment() && videoSources.size() <= metaInfo.getMaxFragment();
    }

    public Timeline buildTimeline(List<VideoFragment> videoSources) throws UnsuitableException {
        if (script == null) {
            return null;
        }
        MetaInfo metaInfo = script.getMeta();

        BasePattern pattern = SelectPatternFactory.getPattern(metaInfo.getPattern());
        if (pattern == null) {
            return null;
        }
        Timeline timeline = obtainFullTimeline();
        return pattern.selectVideos(videoSources, script, timeline);
    }

    private Timeline obtainFullTimeline() {
        Timeline timeline = new SourceTimeline();
        timeline.addMediaTrack(new Track(true, LAYER_SOURCE));
        timeline.addMediaTrack(new Track(true, LAYER_TRANSITION));
        timeline.addMediaTrack(new Track(true, LAYER_FILTER));
        timeline.addMediaTrack(new Track(true, LAYER_OVERLAY));
        return timeline;
    }

    @Override
    public void onCacheSuccess(String url, String cachePath) {
        updateVerifiedResource(url, true);
    }

    @Override
    public void onCacheFailed(String url) {
        updateVerifiedResource(url, false);
    }

    private void updateVerifiedResource(String url, boolean result) {
        if (resourceVerification.containsKey(url)) {
            resourceVerification.put(url, result);
        }
        if (result) {
            boolean verified = true;
            for (Boolean r : resourceVerification.values()) {
                verified &= (r != null ? r : false);
            }
            if (verified) {
                if (listener != null) {
                    eventHandler.post(() -> listener.onScriptResourceVerifyCompleted());
                }
            }
        } else {
            if (listener != null) {
                eventHandler.post(() -> listener.onScriptResourceVerifyFailed(url));
            }
        }
    }

    static class SelectPatternFactory {
        private static Context context;
        private static Map<String, BasePattern> selectPatternMap = new HashMap<>();

        static void setup(Context context) {
            SelectPatternFactory.context = context;
        }

        static void release() {
            SelectPatternFactory.context = null;
        }

        static BasePattern getPattern(String name) {
            if (selectPatternMap.containsKey(name)) {
                return selectPatternMap.get(name);
            }
            BasePattern pattern = null;
            switch (name) {
                case PATTERN_ALL:
                    pattern = new PatternAll(ResourceManager.getInstance(context));
                    break;
                case PATTERN_RANOM:
                    pattern = new PatternRandom(ResourceManager.getInstance(context));
                    break;
                case PATTERN_LOOP:
                    pattern = new PatternLoop(ResourceManager.getInstance(context));
                    break;
                case PATTERN_SEQUENCE:
                    pattern = new PatternSequence(ResourceManager.getInstance(context));
                    break;
                default:
                    pattern = null;
            }
            if (pattern != null) {
                selectPatternMap.put(name, pattern);
            }
            return pattern;
        }
    }

    public interface EventListener {
        void onScriptResourceVerifyCompleted();

        void onScriptResourceVerifyFailed(String failedResUrl);
    }
}
