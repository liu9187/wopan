package cn.com.fanone.wopan.demo.web;

public record WoPanConfigRequest(
        String accessToken,
        String refreshToken,
        Boolean autoRefresh,
        String parentId,
        String spaceType,
        String familyId,
        String privateSpacePassword,
        String baseUrl,
        String zoneUrl
) {
}
