package com.gotokeep.keep.social.composer.filter;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-25 12:16
 */
public interface ExternalFilterFactory {
    boolean hasFilter(String name);

    ExternalFilter createExternalFilter(String name);
}
