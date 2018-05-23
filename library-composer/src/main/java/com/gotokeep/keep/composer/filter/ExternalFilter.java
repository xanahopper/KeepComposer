package com.gotokeep.keep.composer.filter;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 09:54
 */
public interface ExternalFilter {
    void init();

    void destory();

    void onInputSizeChanged(int width, int height);

    int onDrawFrame(int textureId);


}
