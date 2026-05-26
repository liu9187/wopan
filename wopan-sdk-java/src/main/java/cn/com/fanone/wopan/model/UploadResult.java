package cn.com.fanone.wopan.model;

public final class UploadResult {
    private final String fid;
    private final String batchNo;

    public UploadResult(String fid, String batchNo) {
        this.fid = fid;
        this.batchNo = batchNo;
    }

    public String getFid() {
        return fid;
    }

    public String getBatchNo() {
        return batchNo;
    }
}
