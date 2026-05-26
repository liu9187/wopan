package cn.com.fanone.wopan;

final class WoPanConstants {

    private WoPanConstants() {}

    static final String DEFAULT_CLIENT_ID = "1001000021";
    static final String DEFAULT_CLIENT_SECRET = "XFmi9GS2hzk98jGX";
    static final String DEFAULT_APP_ID = "10000001";
    static final String BASE_URL = "https://panservice.mail.wo.cn";
    static final String DEFAULT_ZONE_URL = "https://tjupload.pan.wo.cn";
    static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36";

    static final int DEFAULT_PAGE_SIZE = 100;
    static final long PART_SIZE = 8L * 1024 * 1024;

    static final String CHANNEL_API_USER = "api-user";
    static final String CHANNEL_WO_HOME = "wohome";
    static final String CHANNEL_WO_CLOUD = "wocloud";

    static final String KEY_PC_WEB_LOGIN = "PcWebLogin";
    static final String KEY_PC_LOGIN_VERIFY_CODE = "PcLoginVerifyCode";
    static final String KEY_APP_REFRESH_TOKEN = "AppRefreshToken";
    static final String KEY_APP_QUERY_USER = "AppQueryUser";
    static final String KEY_FAMILY_USER_CURRENT = "FamilyUserCurrentEncode";
    static final String KEY_QUERY_ALL_FILES = "QueryAllFiles";
    static final String KEY_GET_DOWNLOAD_URL_V2 = "GetDownloadUrlV2";
    static final String KEY_CLASSIFY_RULE = "ClassifyRule";
    static final String KEY_CREATE_DIRECTORY = "CreateDirectory";
    static final String KEY_RENAME = "RenameFileOrDirectory";
    static final String KEY_MOVE = "MoveFile";
    static final String KEY_COPY = "CopyFile";
    static final String KEY_DELETE = "DeleteFile";
    static final String KEY_QUERY_USAGE = "QueryCloudUsageInfo";
    static final String KEY_GET_ZONE_INFO = "GetZoneInfo";
    static final String KEY_UPLOAD_2C = "upload2C";
    static final String KEY_PRIVATE_SPACE_LOGIN = "PrivateSpaceLogin";

    static final String HEADER_ORIGIN = "https://pan.wo.cn";
}
