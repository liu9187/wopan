package cn.com.fanone.wopan.model;

public final class UsageInfo {
    private final long totalBytes;
    private final long usedBytes;

    public UsageInfo(long totalBytes, long usedBytes) {
        this.totalBytes = totalBytes;
        this.usedBytes = usedBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public long getUsedBytes() {
        return usedBytes;
    }
}
