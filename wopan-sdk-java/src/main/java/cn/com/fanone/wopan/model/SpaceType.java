package cn.com.fanone.wopan.model;

public enum SpaceType {
    PERSONAL("0"),
    FAMILY("1"),
    PRIVATE("4");

    private final String code;

    SpaceType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
