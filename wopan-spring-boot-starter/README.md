# WoPan Spring Boot Starter

在 Spring Boot 3 / Java 17 应用中引入 starter 后，可以直接注入
`WoPanClient`，无需自行声明 `@Bean`。

## 本地安装

在当前源码工作区先执行：

```bash
mvn -q -pl wopan-spring-boot-starter -am install
```

发布到私服或 Maven 仓库后，业务应用无需执行此步骤。

## 引入依赖

```xml
<dependency>
    <groupId>cn.com.fanone.wopan</groupId>
    <artifactId>wopan-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## 配置

```yaml
wopan:
  access-token: ${WOPAN_ACCESS_TOKEN:}
  refresh-token: ${WOPAN_REFRESH_TOKEN:}
  auto-refresh: true
```

不设置 token 也会创建客户端，可用于调用短信登录流程后获取 token。

可选配置如下：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `wopan.enabled` | `true` | 是否创建默认 `WoPanClient` Bean |
| `wopan.auto-refresh` | `true` | token 过期时自动刷新 |
| `wopan.private-space-password` | 空 | 私密空间密码 |
| `wopan.base-url` | SDK 默认值 | dispatcher API 地址 |
| `wopan.zone-url` | 自动获取 | 上传分区地址 |
| `wopan.user-agent` | SDK 默认值 | HTTP User-Agent |

## 使用

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

应用中自行声明 `WoPanClient` Bean 时，starter 会自动退让；也可以设置
`wopan.enabled=false` 关闭默认客户端。
