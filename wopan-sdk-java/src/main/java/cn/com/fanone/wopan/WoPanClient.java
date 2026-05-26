package cn.com.fanone.wopan;

import cn.com.fanone.wopan.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cn.com.fanone.wopan.model.*;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class WoPanClient implements Closeable {

    private static final DateTimeFormatter BATCH_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final WoPanCrypto crypto;
    private final boolean autoRefresh;
    private final String userAgent;
    private final String baseUrl;

    private volatile String accessToken;
    private volatile String refreshToken;
    private volatile String phoneNumber;
    private volatile String zoneUrl;
    private volatile Map<String, String> extensionToType;
    private volatile String privateSpacePassword;
    private volatile String privateSpaceToken;

    private WoPanClient(Builder builder) {
        this.httpClient = builder.httpClient != null ? builder.httpClient : HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom().setRedirectsEnabled(true).build()).build();
        this.objectMapper = builder.objectMapper != null ? builder.objectMapper : new ObjectMapper();
        this.crypto = new WoPanCrypto(objectMapper);
        this.autoRefresh = builder.autoRefresh;
        this.userAgent = builder.userAgent;
        this.baseUrl = builder.baseUrl;
        this.accessToken = builder.accessToken;
        this.refreshToken = builder.refreshToken;
        this.zoneUrl = builder.zoneUrl;
        this.privateSpacePassword = builder.privateSpacePassword;
    }

    public static Builder builder() {
        return new Builder();
    }

    // region Authentication

    public LoginChallenge loginWithPassword(String phone, String password) {
        return loginWithPassword(phone, password, "", "");
    }

    public LoginChallenge loginWithPassword(String phone, String password, String verifyCode, String uuid) {
        Map<String, Object> param = new HashMap<>();
        param.put("phone", phone);
        param.put("password", password);
        param.put("uuid", normalize(uuid));
        param.put("verifyCode", normalize(verifyCode));
        param.put("clientSecret", WoPanConstants.DEFAULT_CLIENT_SECRET);
        JsonNode data = invoke(WoPanConstants.CHANNEL_API_USER, WoPanConstants.KEY_PC_WEB_LOGIN, param, clientSecretBody(), true);
        boolean needSms = data.path("needSmsCode").asText("1").equals("1");
        return new LoginChallenge(needSms);
    }

    public TokenPair confirmLoginWithSms(String phone, String password, String smsCode) {
        return confirmLoginWithSms(phone, password, smsCode, "", "");
    }

    public TokenPair confirmLoginWithSms(String phone, String password, String smsCode, String verifyCode, String uuid) {
        Map<String, Object> param = new HashMap<>();
        param.put("phone", phone);
        param.put("password", password);
        param.put("messageCode", smsCode);
        param.put("verifyCode", normalize(verifyCode));
        param.put("uuid", normalize(uuid));
        param.put("clientSecret", WoPanConstants.DEFAULT_CLIENT_SECRET);
        JsonNode data = invoke(WoPanConstants.CHANNEL_API_USER, WoPanConstants.KEY_PC_LOGIN_VERIFY_CODE, param, clientSecretBody(), true);
        TokenPair pair = new TokenPair(data.path("access_token").asText(), data.path("refresh_token").asText(), data.path("expires_in").asInt());
        updateTokens(pair);
        return pair;
    }

    public synchronized TokenPair refreshAccessToken() {
        ensureRefreshToken();
        Map<String, Object> param = new HashMap<>();
        param.put("refreshToken", refreshToken);
        param.put("clientSecret", WoPanConstants.DEFAULT_CLIENT_SECRET);
        JsonNode data = invoke(WoPanConstants.CHANNEL_API_USER, WoPanConstants.KEY_APP_REFRESH_TOKEN, param, clientSecretBody(), false);
        TokenPair pair = new TokenPair(data.path("access_token").asText(), data.path("refresh_token").asText(), data.path("expires_in").asInt());
        updateTokens(pair);
        return pair;
    }

    private synchronized void ensurePhoneLoaded() {
        if (phoneNumber != null) {
            return;
        }
        JsonNode data = invoke(WoPanConstants.CHANNEL_API_USER, WoPanConstants.KEY_APP_QUERY_USER, Map.of("accessToken", requireAccessToken()), clientSecretBody(), true);
        phoneNumber = data.path("userId").asText();
    }

    private void updateTokens(TokenPair pair) {
        this.accessToken = pair.getAccessToken();
        this.refreshToken = pair.getRefreshToken();
    }

    private String normalize(String value) {
        return value == null ? "" : value;
    }

    // endregion

    public List<FileItem> listFiles(String parentId, SpaceType spaceType, String familyId, SortRule sortRule, int pageNum, int pageSize) {
        requireAccessToken();
        Map<String, Object> param = new HashMap<>();
        param.put("spaceType", spaceType.getCode());
        param.put("parentDirectoryId", parentId);
        param.put("pageNum", pageNum);
        param.put("pageSize", pageSize);
        param.put("sortRule", sortRule.getCode());
        param.put("clientId", WoPanConstants.DEFAULT_CLIENT_ID);
        if (spaceType == SpaceType.FAMILY) {
            param.put("familyId", familyId != null ? familyId : String.valueOf(getDefaultFamilyId()));
        }
        if (spaceType == SpaceType.PRIVATE) {
            ensurePrivateSpaceToken();
            param.put("psToken", privateSpaceToken);
        }
        JsonNode data = invoke(WoPanConstants.CHANNEL_WO_HOME, WoPanConstants.KEY_QUERY_ALL_FILES, param, secretBody(), true);
        List<FileItem> items = new ArrayList<>();
        JsonNode files = data.path("files");
        if (files.isArray()) {
            files.forEach(node -> items.add(toFileItem(node)));
        }
        return items;
    }

    public List<DownloadLink> getDownloadLinks(List<String> fidList) {
        requireAccessToken();
        Map<String, Object> param = new HashMap<>();
        param.put("type", "1");
        param.put("fidList", fidList);
        param.put("clientId", WoPanConstants.DEFAULT_CLIENT_ID);
        JsonNode data = invoke(WoPanConstants.CHANNEL_WO_HOME, WoPanConstants.KEY_GET_DOWNLOAD_URL_V2, param, secretBody(), true);
        List<DownloadLink> links = new ArrayList<>();
        JsonNode list = data.path("list");
        if (list.isArray()) {
            list.forEach(node -> links.add(new DownloadLink(node.path("fid").asText(), node.path("downloadUrl").asText())));
        }
        return links;
    }

    public DirectoryInfo createDirectory(SpaceType spaceType, String parentId, String name, String familyId) {
        requireAccessToken();
        Map<String, Object> param = new HashMap<>();
        param.put("spaceType", spaceType.getCode());
        param.put("parentDirectoryId", parentId);
        param.put("directoryName", name);
        param.put("clientId", WoPanConstants.DEFAULT_CLIENT_ID);
        if (spaceType == SpaceType.FAMILY) {
            param.put("familyId", familyId != null ? familyId : String.valueOf(getDefaultFamilyId()));
        }
        if (spaceType == SpaceType.PRIVATE) {
            ensurePrivateSpaceToken();
            param.put("psToken", privateSpaceToken);
        }
        JsonNode data = invoke(WoPanConstants.CHANNEL_WO_HOME, WoPanConstants.KEY_CREATE_DIRECTORY, param, secretBody(), true);
        return new DirectoryInfo(data.path("id").asText());
    }

    public void rename(String id, boolean isDir, String newName, SpaceType spaceType, String familyId) {
        requireAccessToken();
        Map<String, Object> param = new HashMap<>();
        param.put("spaceType", spaceType.getCode());
        param.put("type", isDir ? 0 : 1);
        param.put("fileType", isDir ? "0" : determineFileType(newName));
        param.put("id", id);
        param.put("name", newName);
        param.put("clientId", WoPanConstants.DEFAULT_CLIENT_ID);
        if (spaceType == SpaceType.FAMILY) {
            param.put("familyId", familyId != null ? familyId : String.valueOf(getDefaultFamilyId()));
        }
        JsonNode ignore = invoke(WoPanConstants.CHANNEL_WO_HOME, WoPanConstants.KEY_RENAME, param, secretBody(), true);
    }

    public void delete(List<String> dirIds, List<String> fileIds, SpaceType spaceType) {
        requireAccessToken();
        Map<String, Object> param = new HashMap<>();
        param.put("spaceType", spaceType.getCode());
        param.put("vipLevel", "0");
        param.put("dirList", dirIds);
        param.put("fileList", fileIds);
        param.put("clientId", WoPanConstants.DEFAULT_CLIENT_ID);
        invoke(WoPanConstants.CHANNEL_WO_HOME, WoPanConstants.KEY_DELETE, param, secretBody(), true);
    }

    public UsageInfo usageInfo() {
        ensurePhoneLoaded();
        Map<String, Object> param = new HashMap<>();
        param.put("phoneNum", phoneNumber);
        param.put("clientId", WoPanConstants.DEFAULT_CLIENT_ID);
        JsonNode data = invoke(WoPanConstants.CHANNEL_WO_HOME, WoPanConstants.KEY_QUERY_USAGE, param, secretBody(), true);
        long used = data.path("usageInfo").path("byteUsedSize").asLong();
        long total = data.path("usageInfo").path("byteTotalSize").asLong();
        return new UsageInfo(total, used);
    }

    public FamilyInfo getFamilyInfo() {
        JsonNode data = invoke(WoPanConstants.CHANNEL_WO_HOME, WoPanConstants.KEY_FAMILY_USER_CURRENT, Map.of("clientId", WoPanConstants.DEFAULT_CLIENT_ID), secretBody(), true);
        int defaultHomeId = data.path("defaultHomeId").asInt();
        String defaultName = data.path("defaultHomeName").asText();
        return new FamilyInfo(defaultHomeId, defaultName);
    }

    public UploadResult uploadFile(UploadRequest request) {
        requireAccessToken();
        ensureZoneUrl();
        Map<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("spaceType", request.getSpaceType().getCode());
        fileInfo.put("directoryId", request.getParentId());
        String batchNo = BATCH_NO_FORMATTER.format(LocalDateTime.now());
        fileInfo.put("batchNo", batchNo);
        fileInfo.put("fileName", request.getFileName());
        fileInfo.put("fileSize", request.getSize());
        fileInfo.put("fileType", determineFileType(request.getFileName()));
        if (request.getSpaceType() == SpaceType.FAMILY) {
            fileInfo.put("familyId", request.getFamilyId() != null ? request.getFamilyId() : String.valueOf(getDefaultFamilyId()));
        }
        if (request.getSpaceType() == SpaceType.PRIVATE) {
            ensurePrivateSpaceToken();
            fileInfo.put("psToken", privateSpaceToken);
        }
        String encryptedInfo = crypto.encryptParam(WoPanConstants.CHANNEL_WO_HOME, fileInfo, accessToken);

        long totalParts = Math.max(1, (request.getSize() + WoPanConstants.PART_SIZE - 1) / WoPanConstants.PART_SIZE);
        long uploaded = 0L;
        String fid = null;
        InputStream content = request.getContent();
        for (long partIndex = 1; partIndex <= totalParts; partIndex++) {
            int partSize = (int) Math.min(WoPanConstants.PART_SIZE, request.getSize() - uploaded);
            byte[] chunk = readChunk(content, partSize);
            if (chunk.length == 0) {
                break;
            }
            fid = uploadChunk(chunk, partIndex, totalParts, request, encryptedInfo);
            uploaded += chunk.length;
            UploadRequest.ProgressListener listener = request.getProgressListener();
            if (listener != null) {
                listener.onProgress(uploaded, request.getSize());
            }
        }
        return new UploadResult(fid, batchNo);
    }

    private String uploadChunk(byte[] chunk, long partIndex, long totalParts, UploadRequest request, String fileInfo) {
        String uniqueId = System.currentTimeMillis() + "_" + randomAlpha(6);
        int attempts = 0;
        Exception lastError = null;
        while (attempts <= request.getRetryTimes()) {
            attempts++;
            HttpPost post = buildUploadPost(uniqueId, request, fileInfo, chunk, partIndex, totalParts);
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                String body = readBody(response.getEntity());
                JsonNode node = parseJson(body);
                if (!"0000".equals(node.path("code").asText())) {
                    throw new WoPanException("Upload failed: " + node.path("msg").asText());
                }
                return node.path("data").path("fid").asText();
            } catch (IOException ex) {
                lastError = ex;
                UploadRequest.RetryListener retryListener = request.getRetryListener();
                if (retryListener != null) {
                    retryListener.onRetry(attempts, (partIndex - 1) * WoPanConstants.PART_SIZE, ex);
                }
                if (attempts > request.getRetryTimes()) {
                    break;
                }
            }
        }
        throw new WoPanException("Failed to upload part " + partIndex, lastError);
    }

    private HttpPost buildUploadPost(String uniqueId, UploadRequest request, String fileInfo, byte[] chunk, long partIndex, long totalParts) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("uniqueId", uniqueId);
        builder.addTextBody("accessToken", requireAccessToken());
        builder.addTextBody("fileName", request.getFileName());
        builder.addTextBody("psToken", privateSpaceToken != null ? privateSpaceToken : "undefined");
        builder.addTextBody("fileSize", String.valueOf(request.getSize()));
        builder.addTextBody("totalPart", String.valueOf(totalParts));
        builder.addTextBody("channel", WoPanConstants.CHANNEL_WO_CLOUD);
        builder.addTextBody("directoryId", request.getParentId());
        builder.addTextBody("fileInfo", fileInfo);
        builder.addTextBody("partSize", String.valueOf(chunk.length));
        builder.addTextBody("partIndex", String.valueOf(partIndex));
        builder.addBinaryBody("file", chunk, ContentType.create(request.getContentType()), request.getFileName());

        HttpPost post = new HttpPost(zoneUrl() + "/openapi/client/" + WoPanConstants.KEY_UPLOAD_2C);
        post.setHeader("Origin", WoPanConstants.HEADER_ORIGIN);
        post.setHeader("Referer", WoPanConstants.HEADER_ORIGIN + "/");
        post.setHeader("User-Agent", userAgent);
        post.setHeader("Accesstoken", requireAccessToken());
        post.setEntity(builder.build());
        return post;
    }

    private byte[] readChunk(InputStream stream, int expected) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(expected);
            byte[] tmp = new byte[8192];
            int read;
            int remaining = expected;
            while (remaining > 0 && (read = stream.read(tmp, 0, Math.min(tmp.length, remaining))) != -1) {
                buffer.write(tmp, 0, read);
                remaining -= read;
            }
            return buffer.toByteArray();
        } catch (IOException ex) {
            throw new WoPanException("Unable to read upload stream", ex);
        }
    }

    private FileItem toFileItem(JsonNode node) {
        return new FileItem(
                node.path("id").asText(),
                node.path("fid").asText(),
                node.path("type").asInt() == 0,
                node.path("size").asLong(),
                node.path("createTime").asText(),
                node.path("name").asText(),
                node.path("thumbUrl").asText(),
                node.path("type").asInt(),
                node.path("fileType").asText()
        );
    }

    private JsonNode invoke(String channel, String key, Map<String, Object> param, Map<String, Object> other, boolean allowRetry) {
        try {
            Map<String, Object> header = new HashMap<>();
            long resTime = System.currentTimeMillis();
            int reqSeq = ThreadLocalRandom.current().nextInt(100000, 999999);
            header.put("key", key);
            header.put("resTime", resTime);
            header.put("reqSeq", reqSeq);
            header.put("channel", channel);
            header.put("sign", md5(key + resTime + reqSeq + channel));
            header.put("version", "");

            Map<String, Object> body = new HashMap<>(other != null ? other : Map.of());
            if (param != null) {
                String encrypted = crypto.encryptParam(channel, param, accessToken);
                body.put("param", encrypted);
            }

            ObjectNode request = objectMapper.createObjectNode();
            request.set("header", objectMapper.valueToTree(header));
            request.set("body", objectMapper.valueToTree(body));

            HttpPost post = new HttpPost(baseUrl + "/" + channel + "/dispatcher");
            post.setHeader("Content-Type", "application/json;charset=UTF-8");
            post.setHeader("Origin", WoPanConstants.HEADER_ORIGIN);
            post.setHeader("Referer", WoPanConstants.HEADER_ORIGIN + "/");
            post.setHeader("User-Agent", userAgent);
            if (accessToken != null) {
                post.setHeader("Accesstoken", accessToken);
            }
            post.setEntity(new StringEntity(request.toString(), ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(post)) {
                String bodyString = readBody(response.getEntity());
                JsonNode root = objectMapper.readTree(bodyString);
                if (!"200".equals(root.path("STATUS").asText())) {
                    throw new WoPanException("HTTP status rejected: " + root.path("MSG").asText());
                }
                JsonNode rsp = root.path("RSP");
                String code = rsp.path("RSP_CODE").asText();
                if (!"0000".equals(code)) {
                    if (allowRetry && autoRefresh && WoPanConstants.CHANNEL_API_USER.equals(channel) == false && "9999".equals(code)) {
                        refreshAccessToken();
                        return invoke(channel, key, param, other, false);
                    }
                    throw new WoPanException("API error: " + code + " - " + rsp.path("RSP_DESC").asText());
                }
                JsonNode dataNode = rsp.path("DATA");
                if (dataNode.isTextual()) {
                    String decrypted = crypto.decryptPayload(channel, dataNode.asText(), accessToken);
                    if (decrypted == null || decrypted.isBlank()) {
                        return objectMapper.nullNode();
                    }
                    return objectMapper.readTree(decrypted);
                }
                return dataNode;
            }
        } catch (IOException ex) {
            throw new WoPanException("Request failed", ex);
        }
    }

    private void ensureZoneUrl() {
        if (zoneUrl != null) {
            return;
        }
        Map<String, Object> param = Map.of("appId", WoPanConstants.DEFAULT_APP_ID);
        JsonNode data = invoke(WoPanConstants.CHANNEL_WO_HOME, WoPanConstants.KEY_GET_ZONE_INFO, param, Map.of("key", true), true);
        zoneUrl = data.path("url").asText(WoPanConstants.DEFAULT_ZONE_URL);
    }

    private String zoneUrl() {
        ensureZoneUrl();
        return zoneUrl != null ? zoneUrl : WoPanConstants.DEFAULT_ZONE_URL;
    }

    private void ensurePrivateSpaceToken() {
        if (privateSpaceToken != null || privateSpacePassword == null) {
            return;
        }
        Map<String, Object> param = new HashMap<>();
        param.put("pwd", privateSpacePassword);
        param.put("clientId", WoPanConstants.DEFAULT_CLIENT_ID);
        JsonNode data = invoke(WoPanConstants.CHANNEL_WO_HOME, WoPanConstants.KEY_PRIVATE_SPACE_LOGIN, param, secretBody(), true);
        privateSpaceToken = data.path("psToken").asText();
    }

    private int getDefaultFamilyId() {
        return getFamilyInfo().getDefaultHomeId();
    }

    private Map<String, Object> clientSecretBody() {
        Map<String, Object> map = new HashMap<>();
        map.put("clientId", WoPanConstants.DEFAULT_CLIENT_ID);
        map.put("secret", true);
        return map;
    }

    private Map<String, Object> secretBody() {
        return Map.of("secret", true);
    }

    private String determineFileType(String name) {
        ensureClassifyRule();
        if (name == null || !name.contains(".")) {
            return "5";
        }
        String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return extensionToType.getOrDefault(ext, "5");
    }

    private synchronized void ensureClassifyRule() {
        if (extensionToType != null) {
            return;
        }
        JsonNode data = invoke(WoPanConstants.CHANNEL_WO_HOME, WoPanConstants.KEY_CLASSIFY_RULE, Map.of(), Map.of("key", true), true);
        Map<String, String> mapping = new HashMap<>();
        JsonNode fileTypes = data.path("fileTypes");
        fileTypes.fields().forEachRemaining(entry -> mapping.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue().path("type").asText("5")));
        extensionToType = mapping;
    }

    private void ensureRefreshToken() {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new WoPanException("Refresh token not configured");
        }
    }

    private String requireAccessToken() {
        if (accessToken == null || accessToken.isBlank()) {
            throw new WoPanException("Access token not configured. Login first or provide an existing token.");
        }
        return accessToken;
    }

    private String readBody(HttpEntity entity) throws IOException {
        if (entity == null) {
            return "";
        }
        try (InputStream stream = entity.getContent()) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private JsonNode parseJson(String body) throws JsonProcessingException {
        return objectMapper.readTree(body);
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
                return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new WoPanException("MD5 algorithm missing", ex);
        }
    }

    private String randomAlpha(int len) {
        Random random = ThreadLocalRandom.current();
        String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }

    public static final class Builder {
        private CloseableHttpClient httpClient;
        private ObjectMapper objectMapper;
        private boolean autoRefresh = true;
        private String userAgent = WoPanConstants.DEFAULT_USER_AGENT;
        private String accessToken;
        private String refreshToken;
        private String baseUrl = WoPanConstants.BASE_URL;
        private String zoneUrl;
        private String privateSpacePassword;

        public Builder httpClient(CloseableHttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder objectMapper(ObjectMapper mapper) {
            this.objectMapper = mapper;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder autoRefresh(boolean autoRefresh) {
            this.autoRefresh = autoRefresh;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder zoneUrl(String zoneUrl) {
            this.zoneUrl = zoneUrl;
            return this;
        }

        public Builder privateSpacePassword(String password) {
            this.privateSpacePassword = password;
            return this;
        }

        public WoPanClient build() {
            return new WoPanClient(this);
        }
    }

}
