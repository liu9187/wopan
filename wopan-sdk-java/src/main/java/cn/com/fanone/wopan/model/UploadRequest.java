package cn.com.fanone.wopan.model;

import java.io.InputStream;

public final class UploadRequest {

    private final SpaceType spaceType;
    private final String parentId;
    private final String familyId;
    private final String fileName;
    private final InputStream content;
    private final long size;
    private final String contentType;
    private final int retryTimes;
    private final ProgressListener progressListener;
    private final RetryListener retryListener;

    private UploadRequest(Builder builder) {
        this.spaceType = builder.spaceType;
        this.parentId = builder.parentId;
        this.familyId = builder.familyId;
        this.fileName = builder.fileName;
        this.content = builder.content;
        this.size = builder.size;
        this.contentType = builder.contentType;
        this.retryTimes = builder.retryTimes;
        this.progressListener = builder.progressListener;
        this.retryListener = builder.retryListener;
    }

    public SpaceType getSpaceType() {
        return spaceType;
    }

    public String getParentId() {
        return parentId;
    }

    public String getFamilyId() {
        return familyId;
    }

    public String getFileName() {
        return fileName;
    }

    public InputStream getContent() {
        return content;
    }

    public long getSize() {
        return size;
    }

    public String getContentType() {
        return contentType;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public ProgressListener getProgressListener() {
        return progressListener;
    }

    public RetryListener getRetryListener() {
        return retryListener;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private SpaceType spaceType = SpaceType.PERSONAL;
        private String parentId = "0";
        private String familyId;
        private String fileName;
        private InputStream content;
        private long size;
        private String contentType = "application/octet-stream";
        private int retryTimes = 2;
        private ProgressListener progressListener;
        private RetryListener retryListener;

        public Builder spaceType(SpaceType spaceType) {
            this.spaceType = spaceType;
            return this;
        }

        public Builder parentId(String parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder familyId(String familyId) {
            this.familyId = familyId;
            return this;
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder content(InputStream content) {
            this.content = content;
            return this;
        }

        public Builder size(long size) {
            this.size = size;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder retryTimes(int retryTimes) {
            this.retryTimes = retryTimes;
            return this;
        }

        public Builder onProgress(ProgressListener listener) {
            this.progressListener = listener;
            return this;
        }

        public Builder onRetry(RetryListener listener) {
            this.retryListener = listener;
            return this;
        }

        public UploadRequest build() {
            if (fileName == null || fileName.isBlank()) {
                throw new IllegalArgumentException("fileName is required");
            }
            if (content == null) {
                throw new IllegalArgumentException("content stream is required");
            }
            if (size <= 0) {
                throw new IllegalArgumentException("size must be positive");
            }
            return new UploadRequest(this);
        }
    }

    @FunctionalInterface
    public interface ProgressListener {
        void onProgress(long uploaded, long total);
    }

    @FunctionalInterface
    public interface RetryListener {
        void onRetry(int attempt, long uploadedBytes, Exception error);
    }
}
