package com.gotokeep.keep.director;

import android.support.annotation.NonNull;

import com.gotokeep.keep.composer.timeline.Timeline;
import com.gotokeep.keep.director.data.DirectorScript;
import com.gotokeep.keep.director.exception.UnsuitableException;

import java.util.List;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 14:17
 */
public interface SelectPattern {
    Timeline selectVideos(@NonNull List<VideoFragment> videoSources, DirectorScript script) throws UnsuitableException;
}
