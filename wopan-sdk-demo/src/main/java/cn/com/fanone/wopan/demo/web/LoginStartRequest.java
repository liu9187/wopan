package cn.com.fanone.wopan.demo.web;

public record LoginStartRequest(
        String phone,
        String password,
        String verifyCode,
        String uuid
) {
}
