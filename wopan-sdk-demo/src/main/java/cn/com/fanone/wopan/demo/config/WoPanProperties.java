package cn.com.fanone.wopan.demo.config;

import cn.com.fanone.wopan.model.SpaceType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wopan")
public class WoPanProperties {

    private String accessToken = "";
    private String refreshToken = "";
    private boolean autoRefresh = true;
    private String parentId = "0";
    private SpaceType spaceType = SpaceType.PERSONAL;
    private String familyId = "";
    private String privateSpacePassword = "";
    private String baseUrl = "";
    private String zoneUrl = "";

    public WoPanRuntimeConfig toRuntimeConfig() {
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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public SpaceType getSpaceType() {
        return spaceType;
    }

    public void setSpaceType(SpaceType spaceType) {
        this.spaceType = spaceType;
    }

    public String getFamilyId() {
        return familyId;
    }

    public void setFamilyId(String familyId) {
        this.familyId = familyId;
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
}
