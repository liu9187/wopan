package cn.com.fanone.wopan.demo.config;

import cn.com.fanone.wopan.demo.web.WoPanConfigRequest;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class WoPanConfigStore {

    private final AtomicReference<WoPanRuntimeConfig> current;

    public WoPanConfigStore(WoPanProperties properties) {
        this.current = new AtomicReference<>(properties.toRuntimeConfig());
    }

    public WoPanRuntimeConfig current() {
        return current.get();
    }

    public WoPanRuntimeConfig update(WoPanConfigRequest request) {
        return current.updateAndGet(config -> config.merge(request));
    }

    public WoPanRuntimeConfig updateTokens(String accessToken, String refreshToken) {
        return current.updateAndGet(config -> config.withTokens(accessToken, refreshToken));
    }
}
