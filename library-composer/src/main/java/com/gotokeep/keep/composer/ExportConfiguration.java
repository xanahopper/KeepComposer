package com.gotokeep.keep.composer;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 15:07
 */
public final class ExportConfiguration {
    private int width;
    private int height;
    private int bitRate;
    private int frameRate;
    private int keyFrameInterval;

    private ExportConfiguration(Builder builder) {
        width = builder.width;
        height = builder.height;
        bitRate = builder.bitRate;
        frameRate = builder.frameRate;
        keyFrameInterval = builder.keyFrameInterval;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getBitRate() {
        return bitRate;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public int getKeyFrameInterval() {
        return keyFrameInterval;
    }


    public static final class Builder {
        private int width;
        private int height;
        private int bitRate;
        private int frameRate;
        private int keyFrameInterval;

        private Builder() {
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setBitRate(int bitRate) {
            this.bitRate = bitRate;
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

        public ExportConfiguration build() {
            return new ExportConfiguration(this);
        }
    }
}
