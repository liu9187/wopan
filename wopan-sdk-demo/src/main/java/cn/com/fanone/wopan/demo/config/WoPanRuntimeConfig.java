package cn.com.fanone.wopan.demo.config;

import cn.com.fanone.wopan.WoPanClient;
import cn.com.fanone.wopan.model.SpaceType;
import cn.com.fanone.wopan.demo.web.WoPanConfigRequest;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public record WoPanRuntimeConfig(
        String accessToken,
        String refreshToken,
        boolean autoRefresh,
        String parentId,
        SpaceType spaceType,
        String familyId,
        String privateSpacePassword,
        String baseUrl,
        String zoneUrl
) {
    public WoPanRuntimeConfig merge(WoPanConfigRequest request) {
        return new WoPanRuntimeConfig(
                updateSecret(request.accessToken(), accessToken),
                updateSecret(request.refreshToken(), refreshToken),
                request.autoRefresh() != null ? request.autoRefresh() : autoRefresh,
                text(request.parentId(), parentId, "0"),
                parseSpaceType(text(request.spaceType(), spaceType.name(), "PERSONAL")),
                text(request.familyId(), familyId, ""),
                updateSecret(request.privateSpacePassword(), privateSpacePassword),
                text(request.baseUrl(), baseUrl, ""),
                text(request.zoneUrl(), zoneUrl, "")
        );
    }

    public WoPanRuntimeConfig withTokens(String accessToken, String refreshToken) {
        return new WoPanRuntimeConfig(
                accessToken,
                refreshToken,
                autoRefresh,
                parentId,
                spaceType,
                familyId,
                privateSpacePassword,
                baseUrl,
                zoneUrl
        );
    }

    public boolean isUsable() {
        return !isBlank(accessToken) && !isBlank(refreshToken);
    }

    public WoPanClient newClient() {
        WoPanClient.Builder builder = WoPanClient.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .autoRefresh(autoRefresh);
        if (!isBlank(privateSpacePassword)) {
            builder.privateSpacePassword(privateSpacePassword);
        }
        if (!isBlank(baseUrl)) {
            builder.baseUrl(baseUrl);
        }
        if (!isBlank(zoneUrl)) {
            builder.zoneUrl(zoneUrl);
        }
        return builder.build();
    }

    public Map<String, Object> toPublicMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("configured", isUsable());
        map.put("accessToken", mask(accessToken));
        map.put("refreshToken", mask(refreshToken));
        map.put("autoRefresh", autoRefresh);
        map.put("parentId", parentId);
        map.put("spaceType", spaceType.name());
        map.put("familyId", familyId);
        map.put("privateSpacePassword", isBlank(privateSpacePassword) ? "" : "******");
        map.put("baseUrl", baseUrl);
        map.put("zoneUrl", zoneUrl);
        return map;
    }

    private static String updateSecret(String value, String current) {
        if (isBlank(value) || value.contains("******")) {
            return current;
        }
        return value.trim();
    }

    private static String text(String value, String current, String fallback) {
        if (value == null) {
            return firstNonBlank(current, fallback);
        }
        return value.trim();
    }

    private static SpaceType parseSpaceType(String raw) {
        try {
            return SpaceType.valueOf(firstNonBlank(raw, "PERSONAL").trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return SpaceType.PERSONAL;
        }
    }

    private static String mask(String value) {
        if (isBlank(value)) {
            return "";
        }
        if (value.length() <= 8) {
            return "******";
        }
        return value.substring(0, 4) + "******" + value.substring(value.length() - 4);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
