package cn.com.fanone.wopan.model;

public final class FamilyInfo {
    private final int defaultHomeId;
    private final String defaultHomeName;

    public FamilyInfo(int defaultHomeId, String defaultHomeName) {
        this.defaultHomeId = defaultHomeId;
        this.defaultHomeName = defaultHomeName;
    }

    public int getDefaultHomeId() {
        return defaultHomeId;
    }

    public String getDefaultHomeName() {
        return defaultHomeName;
    }
}
