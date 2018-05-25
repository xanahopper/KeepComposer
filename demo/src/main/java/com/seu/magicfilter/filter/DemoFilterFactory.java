package com.seu.magicfilter.filter;

import com.gotokeep.keep.composer.filter.ExternalFilter;
import com.gotokeep.keep.composer.filter.ExternalFilterFactory;
import com.seu.magicfilter.filter.advanced.MagicBlackCatFilter;
import com.seu.magicfilter.filter.advanced.MagicSunsetFilter;
import com.seu.magicfilter.filter.base.gpuimage.GPUImageFilter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-25 14:20
 */
public class DemoFilterFactory implements ExternalFilterFactory {
    private static Map<String, Class<? extends GPUImageFilter>> filterPool = new HashMap<>();
    private static DemoFilterFactory instance = null;

    static {
        filterPool.put("blackcat", MagicBlackCatFilter.class);
        filterPool.put("sunset", MagicSunsetFilter.class);
    }

    public static DemoFilterFactory getInstance() {
        if (instance == null) {
            instance = new DemoFilterFactory();
        }
        return instance;
    }

    @Override
    public boolean hasFilter(String name) {
        return filterPool.containsKey(name);
    }

    @Override
    public ExternalFilter createExternalFilter(String name) {
        if (hasFilter(name)) {
            Class<? extends GPUImageFilter> clazz = filterPool.get(name);
            try {
                return clazz.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
