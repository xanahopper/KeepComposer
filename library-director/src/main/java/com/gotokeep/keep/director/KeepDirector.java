package com.gotokeep.keep.director;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.gotokeep.keep.composer.timeline.Timeline;
import com.gotokeep.keep.director.data.Chapter;
import com.gotokeep.keep.director.data.ChapterSet;
import com.gotokeep.keep.director.data.DirectorScript;
import com.gotokeep.keep.director.data.Filter;
import com.gotokeep.keep.director.data.MetaInfo;
import com.gotokeep.keep.director.data.Overlay;
import com.gotokeep.keep.director.exception.UnsuitableException;
import com.gotokeep.keep.director.pattern.BasePattern;
import com.gotokeep.keep.director.pattern.PatternAll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 11:36
 */
public final class KeepDirector {
    public static final String PATTERN_ALL = "all";
    public static final String PATTERN_ALL_ODD = "allOdd";
    public static final String PATTERN_ALL_EVEN = "allEven";
    public static final String PATTERN_RANOM = "random";
    public static final String PATTERN_FIRST = "first";
    public static final String PATTERN_LAST = "last";
    public static final String PATTERN_SEQUENCE = "sequence";
    public static final String PATTERN_LOOP = "loop";

    private ResourceManager resourceManager;
    private DirectorScript script;
    private Gson gson;

    public KeepDirector(Context context) {
        gson = new Gson();
        resourceManager = ResourceManager.getInstance(context);
        SelectPatternFactory.setup(context);
    }

    public KeepDirector(Context context, String scriptText) {
        this(context);
        setScriptText(scriptText);
    }

    public void setScriptText(String scriptText) {
        this.script = gson.fromJson(scriptText, DirectorScript.class);
    }

    public DirectorScript getScript() {
        return script;
    }

    /**
     * 验证并准备此脚本（即下载脚本中所有资源）
     * @return 是否已经准备就绪
     */
    public boolean verifyScript() {
        if (script == null || script.getMeta() == null) {
            return false;
        }
        List<String> resourceList = new ArrayList<>();
        if (!TextUtils.isEmpty(script.getCover())) {
            resourceList.add(script.getCover());
        }
        MetaInfo metaInfo = script.getMeta();
        resourceList.addAll(metaInfo.getFilter().getResources());
        for (Overlay overlay : metaInfo.getOverlay()) {
            resourceList.addAll(overlay.getResources());
        }
        if (!TextUtils.isEmpty(metaInfo.getMusic())) {
            resourceList.add(metaInfo.getMusic());
        }
        if (script.getChapter() != null) {
            ChapterSet chapterSet = script.getChapter();
            if (chapterSet.getHeader() != null) {
                resourceList.addAll(chapterSet.getHeader().getResources());
            }
            if (chapterSet.getFooter() != null) {
                resourceList.addAll(chapterSet.getFooter().getResources());
            }
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
                resourceManager.cacheFiile(resource);
            }
        }
        return verified;
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

        return pattern.selectVideos(videoSources, script);
    }

    static class SelectPatternFactory {
        private static Context context;
        private static Map<String, BasePattern> selectPatternMap = new HashMap<>();

        static void setup(Context context) {
            SelectPatternFactory.context = context;
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
                default:
                    pattern = null;
            }
            if (pattern != null) {
                selectPatternMap.put(name, pattern);
            }
            return pattern;
        }
    }
}
