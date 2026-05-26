package cn.com.fanone.wopan.demo.web;

import cn.com.fanone.wopan.demo.config.WoPanConfigStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class WoPanConfigController {

    private final WoPanConfigStore configStore;

    public WoPanConfigController(WoPanConfigStore configStore) {
        this.configStore = configStore;
    }

    @GetMapping
    public Map<String, Object> getConfig() {
        return configStore.current().toPublicMap();
    }

    @PostMapping
    public Map<String, Object> updateConfig(@RequestBody WoPanConfigRequest request) {
        return configStore.update(request).toPublicMap();
    }
}
