package cn.com.fanone.wopan.demo.web;

public record LoginConfirmRequest(
        String phone,
        String password,
        String smsCode,
        String verifyCode,
        String uuid
) {
}
