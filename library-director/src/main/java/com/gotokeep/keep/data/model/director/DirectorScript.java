package com.gotokeep.keep.data.model.director;

/**
 * 脚本
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 11:36
 */
public class DirectorScript{
    private String type;
    private String version;
    private String id;
    private String name;
    private String label;
    private String cover;
    private MetaInfo meta;
    private Resource header;
    private Chapter footer;
    private ChapterSet chapter;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public MetaInfo getMeta() {
        return meta;
    }

    public void setMeta(MetaInfo meta) {
        this.meta = meta;
    }

    public ChapterSet getChapter() {
        return chapter;
    }

    public void setChapter(ChapterSet chapter) {
        this.chapter = chapter;
    }

    public Resource getHeader() {
        return header;
    }

    public void setHeader(Resource header) {
        this.header = header;
    }

    public Chapter getFooter() {
        return footer;
    }

    public void setFooter(Chapter footer) {
        this.footer = footer;
    }

    @Override
    public String toString() {
        return "DirectorScript{" +
                "type='" + type + '\'' +
                ", version='" + version + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", label='" + label + '\'' +
                ", cover='" + cover + '\'' +
                ", meta=" + meta +
                ", chapter=" + chapter +
                '}';
    }
}
