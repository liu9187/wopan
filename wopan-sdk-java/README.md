# WoPan Java SDK

Java 17 SDK that replicates the official Web API workflow of 中国联通云网盘（WoPan）.  It wraps the encrypted dispatcher APIs so Spring Boot services can authenticate with phone credentials, refresh tokens, list files, fetch download links, create folders, delete items, and stream uploads.

## Features

- PcWebLogin + PcLoginVerifyCode helpers to obtain `access_token` / `refresh_token`
- Automatic AES-CBC encryption/decryption and header signing identical to the browser
- Token refresh & phone/profile bootstrap helpers
- File browsing, download URL retrieval, CRUD operations, and multipart upload (8 MB chunks) with retry hooks
- Builder pattern for framework-neutral configuration
## Quick Start

```bash
mvn -f wopan-sdk-java/pom.xml clean package
```

### Spring Boot configuration

For Spring Boot applications, prefer the companion `wopan-spring-boot-starter`
module, which creates a `WoPanClient` bean from `wopan.*` properties. The
manual builder equivalent is:

```java
@Configuration
public class WoPanConfig {

    @Bean
    public WoPanClient woPanClient() {
        return WoPanClient.builder()
                .accessToken(System.getenv("WOPAN_ACCESS"))
                .refreshToken(System.getenv("WOPAN_REFRESH"))
                .autoRefresh(true)
                .build();
    }
}
```

### Usage example

```java
@Service
public class CloudService {
    private final WoPanClient client;

    public CloudService(WoPanClient client) {
        this.client = client;
    }

    public List<FileItem> listRoot() {
        return client.listFiles("0", SpaceType.PERSONAL, null, SortRule.NAME_ASC, 0, 100);
    }
}
```

## SMS login flow

```java
WoPanClient client = WoPanClient.builder().build();
LoginChallenge challenge = client.loginWithPassword("13800000000", "yourPassword");
// send SMS manually...
TokenPair tokens = client.confirmLoginWithSms("13800000000", "yourPassword", "123456");
```

Once you have refresh/access tokens, persist them securely and feed them to the builder. The SDK refreshes tokens transparently when the API signals `9999`.

## Upload

```java
UploadResult result = client.uploadFile(UploadRequest.builder()
        .spaceType(SpaceType.PERSONAL)
        .parentId("0")
        .fileName("example.zip")
        .familyId(null)
        .content(Files.newInputStream(path))
        .contentType("application/zip")
        .size(Files.size(path))
        .build());
```

`UploadRequest` accepts optional retry strategy callbacks so you can hook logs or metrics.

## Notes

- If the same account logs into https://pan.wo.cn via browser again, WoPan invalidates existing web tokens. Host the token harvesting once, then let the SDK auto-refresh.
- Family cloud operations require `familyId` from `FamilyUserCurrentEncode` (the client caches it for you).
- Private space uploads need `psToken`; set it with `WoPanClient.builder().privateSpacePassword("xxxx")`.

See `WoPanClient` for the full surface area of available methods.
