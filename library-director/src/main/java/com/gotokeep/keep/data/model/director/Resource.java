package com.gotokeep.keep.data.model.director;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/6/4 22:23
 */
public final class Resource {
    private String id;
    private String name;
    private String value;
    private String version;
    private String type;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", version='" + version + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
