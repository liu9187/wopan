package cn.com.fanone.wopan.demo.web;

import cn.com.fanone.wopan.WoPanClient;
import cn.com.fanone.wopan.demo.config.WoPanConfigStore;
import cn.com.fanone.wopan.model.LoginChallenge;
import cn.com.fanone.wopan.model.TokenPair;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/login")
public class WoPanLoginController {

    private static final String CAPTCHA_BASE_URL = "https://panservice.mail.wo.cn/api-user/getverifycode?uuid=";

    private final WoPanConfigStore configStore;

    public WoPanLoginController(WoPanConfigStore configStore) {
        this.configStore = configStore;
    }

    @PostMapping("/start")
    public LoginStartResponse start(@RequestBody LoginStartRequest request) throws Exception {
        validate(request.phone(), "手机号不能为空");
        validate(request.password(), "密码不能为空");
        try (WoPanClient client = WoPanClient.builder().build()) {
            LoginChallenge challenge = client.loginWithPassword(
                    request.phone(),
                    request.password(),
                    request.verifyCode(),
                    request.uuid()
            );
            return new LoginStartResponse(challenge.isNeedSmsCode(), "短信验证码已触发，请查看手机");
        }
    }

    @GetMapping("/captcha")
    public CaptchaResponse captcha() {
        String uuid = UUID.randomUUID().toString();
        return new CaptchaResponse(uuid, CAPTCHA_BASE_URL + uuid);
    }

    @PostMapping("/confirm")
    public LoginConfirmResponse confirm(@RequestBody LoginConfirmRequest request) throws Exception {
        validate(request.phone(), "手机号不能为空");
        validate(request.password(), "密码不能为空");
        validate(request.smsCode(), "短信验证码不能为空");
        try (WoPanClient client = WoPanClient.builder().build()) {
            TokenPair tokenPair = client.confirmLoginWithSms(
                    request.phone(),
                    request.password(),
                    request.smsCode(),
                    request.verifyCode(),
                    request.uuid()
            );
            configStore.updateTokens(tokenPair.getAccessToken(), tokenPair.getRefreshToken());
            return new LoginConfirmResponse(
                    tokenPair.getAccessToken(),
                    tokenPair.getRefreshToken(),
                    tokenPair.getExpiresIn(),
                    configStore.current().toPublicMap()
            );
        }
    }

    private static void validate(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    public record LoginStartResponse(
            boolean needSmsCode,
            String message
    ) {
    }

    public record CaptchaResponse(
            String uuid,
            String imageUrl
    ) {
    }

    public record LoginConfirmResponse(
            String accessToken,
            String refreshToken,
            int expiresIn,
            Map<String, Object> config
    ) {
    }
}
