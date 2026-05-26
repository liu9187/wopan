package cn.com.fanone.wopan.model;

public final class LoginChallenge {
    private final boolean needSmsCode;

    public LoginChallenge(boolean needSmsCode) {
        this.needSmsCode = needSmsCode;
    }

    public boolean isNeedSmsCode() {
        return needSmsCode;
    }
}
