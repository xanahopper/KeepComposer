package com.gotokeep.keep.social.composer.timeline.item;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-17 10:35
 */
public class LayerItem extends OverlayItem {
    private String url;

    public LayerItem(String url) {
        super(0);
        this.url = url;
    }

    public LayerItem(int layer, String name) {
        super(layer);
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
