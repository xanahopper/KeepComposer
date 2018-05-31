package com.gotokeep.keep.social.composer;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 15:07
 */
public final class ExportConfiguration {
    private int width;
    private int height;
    private int videoBitRate;
    private int frameRate;
    private int keyFrameInterval;
    private int audioBitRate;
    private String outputFilePath;

    private ExportConfiguration(Builder builder) {
        setWidth(builder.width);
        setHeight(builder.height);
        setVideoBitRate(builder.videoBitRate);
        setFrameRate(builder.frameRate);
        setKeyFrameInterval(builder.keyFrameInterval);
        setAudioBitRate(builder.audioBitRate);
        setOutputFilePath(builder.outputFilePath);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getVideoBitRate() {
        return videoBitRate;
    }

    public void setVideoBitRate(int videoBitRate) {
        this.videoBitRate = videoBitRate;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    public int getKeyFrameInterval() {
        return keyFrameInterval;
    }

    public void setKeyFrameInterval(int keyFrameInterval) {
        this.keyFrameInterval = keyFrameInterval;
    }

    public int getAudioBitRate() {
        return audioBitRate;
    }

    public void setAudioBitRate(int audioBitRate) {
        this.audioBitRate = audioBitRate;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public void setOutputFilePath(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    public static final class Builder {
        private int width;
        private int height;
        private int videoBitRate;
        private int frameRate;
        private int keyFrameInterval;
        private int audioBitRate;
        private String outputFilePath;

        private Builder() {
        }

        public Builder setVideoSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder setVideoBitRate(int videoBitRate) {
            this.videoBitRate = videoBitRate;
            return this;
        }

        public Builder setFrameRate(int frameRate) {
            this.frameRate = frameRate;
            return this;
        }

        public Builder setKeyFrameInterval(int keyFrameInterval) {
            this.keyFrameInterval = keyFrameInterval;
            return this;
        }

        public Builder setAudioBitRate(int audioBitRate) {
            this.audioBitRate = audioBitRate;
            return this;
        }

        public Builder setOutputFilePath(String outputFilePath) {
            this.outputFilePath = outputFilePath;
            return this;
        }

        public ExportConfiguration build() {
            return new ExportConfiguration(this);
        }
    }
}
