package com.gotokeep.keep.social.composer.filter;

import com.gotokeep.keep.social.composer.filter.basic.KeepExternalFilter;

import java.util.HashSet;
import java.util.Set;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-15 16:34
 */
public final class FilterFactory {
    private static Set<ExternalFilterFactory> externalFilterFactories = new HashSet<>();

    public static void registerExternalFilterFactory(ExternalFilterFactory factory) {
        externalFilterFactories.add(factory);
    }

    public static MediaFilter getFilter(String name) {
        return null;
    }

    public static KeepExternalFilter getExternalFilter(String externalName) {
        ExternalFilter externalFilter = null;
        for (ExternalFilterFactory factory : externalFilterFactories) {
            if (factory.hasFilter(externalName)) {
                externalFilter = factory.createExternalFilter(externalName);
            }
        }
        if (externalFilter != null) {
            return new KeepExternalFilter(externalFilter);
        } else {
            return null;
        }
    }
}
