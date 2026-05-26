package cn.com.fanone.wopan.model;

public final class DownloadLink {
    private final String fid;
    private final String url;

    public DownloadLink(String fid, String url) {
        this.fid = fid;
        this.url = url;
    }

    public String getFid() {
        return fid;
    }

    public String getUrl() {
        return url;
    }
}
