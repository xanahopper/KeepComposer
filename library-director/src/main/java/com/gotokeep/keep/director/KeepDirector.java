package com.gotokeep.keep.director;

import com.google.gson.Gson;
import com.gotokeep.keep.composer.timeline.Timeline;
import com.gotokeep.keep.director.data.DirectorScript;
import com.gotokeep.keep.director.data.MetaInfo;
import com.gotokeep.keep.director.exception.UnsuitableException;
import com.gotokeep.keep.director.pattern.PatternAll;

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

    private DirectorScript script;
    private Gson gson;

    public KeepDirector() {
        gson = new Gson();
    }

    public KeepDirector(String scriptText) {
        this();
        setScriptText(scriptText);
    }

    public void setScriptText(String scriptText) {
        this.script = gson.fromJson(scriptText, DirectorScript.class);
    }

    public DirectorScript getScript() {
        return script;
    }

    public Timeline buildTimeline(List<VideoFragment> videoSources) throws UnsuitableException {
        if (script == null) {
            return null;
        }
        MetaInfo metaInfo = script.getMeta();

        SelectPattern pattern = SelectPatternFactory.getPattern(metaInfo.getPattern());
        if (pattern == null) {
            return null;
        }

        Timeline timeline = pattern.selectVideos(videoSources, script);
        return timeline;
    }

    static class SelectPatternFactory {
        private static Map<String, SelectPattern> selectPatternMap = new HashMap<>();
        static SelectPattern getPattern(String name) {
            if (selectPatternMap.containsKey(name)) {
                return selectPatternMap.get(name);
            }
            SelectPattern pattern = null;
            switch (name) {
                case PATTERN_ALL:
                    pattern = new PatternAll();
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
