package cn.com.fanone.wopan.model;

public enum SortRule {
    NAME_ASC(1),
    NAME_DESC(2),
    SIZE_ASC(3),
    SIZE_DESC(4),
    TIME_ASC(5),
    TIME_DESC(6);

    private final int code;

    SortRule(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
