package cn.com.fanone.wopan.autoconfigure;

import cn.com.fanone.wopan.WoPanClient;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wopan")
public class WoPanProperties {

    /**
     * Whether the WoPan client should be automatically configured.
     */
    private boolean enabled = true;

    /**
     * Access token used for authenticated WoPan API calls.
     */
    private String accessToken;

    /**
     * Refresh token used when an access token expires.
     */
    private String refreshToken;

    /**
     * Whether an expired access token should be refreshed automatically.
     */
    private boolean autoRefresh = true;

    /**
     * Password used for private-space operations.
     */
    private String privateSpacePassword;

    /**
     * Overrides the dispatcher API base URL.
     */
    private String baseUrl;

    /**
     * Overrides the upload zone URL.
     */
    private String zoneUrl;

    /**
     * Overrides the HTTP User-Agent sent by the SDK.
     */
    private String userAgent;

    WoPanClient.Builder applyTo(WoPanClient.Builder builder) {
        builder.autoRefresh(autoRefresh);
        if (hasText(accessToken)) {
            builder.accessToken(accessToken.trim());
        }
        if (hasText(refreshToken)) {
            builder.refreshToken(refreshToken.trim());
        }
        if (hasText(privateSpacePassword)) {
            builder.privateSpacePassword(privateSpacePassword);
        }
        if (hasText(baseUrl)) {
            builder.baseUrl(baseUrl.trim());
        }
        if (hasText(zoneUrl)) {
            builder.zoneUrl(zoneUrl.trim());
        }
        if (hasText(userAgent)) {
            builder.userAgent(userAgent.trim());
        }
        return builder;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    public void setAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }

    public String getPrivateSpacePassword() {
        return privateSpacePassword;
    }

    public void setPrivateSpacePassword(String privateSpacePassword) {
        this.privateSpacePassword = privateSpacePassword;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getZoneUrl() {
        return zoneUrl;
    }

    public void setZoneUrl(String zoneUrl) {
        this.zoneUrl = zoneUrl;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
