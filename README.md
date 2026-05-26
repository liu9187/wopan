# WoPan SDK

中国联通云网盘（WoPan）的 Java 17 SDK 与 Spring Boot 3 Starter。

项目封装了登录、Token 刷新、文件列表、下载链接、目录管理以及分片上传等能力。Spring Boot 应用可直接引入 Starter，通过配置创建 `WoPanClient` Bean。

## 模块说明

| 模块 | 说明 |
| --- | --- |
| `wopan-sdk-java` | 与框架无关的 Java SDK 核心模块 |
| `wopan-spring-boot-starter` | Spring Boot 自动装配模块，推荐业务应用使用 |
| `wopan-sdk-demo` | Web 示例应用，演示登录、配置与图片上传预览 |

## 环境要求

- JDK 17+
- Maven 3.9+
- Spring Boot 3.3.x（使用 Starter 时）

项目基于 Java 17 构建。使用 IntelliJ IDEA 运行 Maven 时，建议将 Maven Runner JRE 设置为 JDK 17；若使用 JDK 24/25，IDE 内置 Maven 依赖可能输出 `sun.misc.Unsafe` 弃用警告，但这不是 SDK 业务代码错误。

## 构建

在项目根目录执行：

```bash
mvn clean package
```

如需将本地快照版本安装到 Maven 仓库，供其他项目引用：

```bash
mvn clean install
```

## Spring Boot 快速接入

引入 Starter：

```xml
<dependency>
    <groupId>cn.com.fanone.wopan</groupId>
    <artifactId>wopan-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

配置 `application.yml`：

```yaml
wopan:
  access-token: ${WOPAN_ACCESS_TOKEN:}
  refresh-token: ${WOPAN_REFRESH_TOKEN:}
  auto-refresh: true
```

直接注入客户端：

```java
import cn.com.fanone.wopan.WoPanClient;
import cn.com.fanone.wopan.model.FileItem;
import cn.com.fanone.wopan.model.SortRule;
import cn.com.fanone.wopan.model.SpaceType;
import org.springframework.stereotype.Service;

import java.util.List;

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

不配置 Token 时，Starter 仍会创建客户端，可先调用短信登录流程获取 Token。应用自行声明 `WoPanClient` Bean 时，自动装配会退让；设置 `wopan.enabled=false` 可关闭默认客户端。

## 配置项

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `wopan.enabled` | `true` | 是否创建默认 `WoPanClient` Bean |
| `wopan.access-token` | 空 | 已登录账号的 Access Token |
| `wopan.refresh-token` | 空 | 用于续期的 Refresh Token |
| `wopan.auto-refresh` | `true` | Token 失效时是否自动刷新 |
| `wopan.private-space-password` | 空 | 私密空间密码 |
| `wopan.base-url` | SDK 默认值 | dispatcher API 地址覆盖值 |
| `wopan.zone-url` | 自动获取 | 上传分区地址覆盖值 |
| `wopan.user-agent` | SDK 默认值 | 请求 User-Agent 覆盖值 |

## 运行 Demo

```bash
mvn -q -pl wopan-sdk-demo -am package
java -jar wopan-sdk-demo/target/wopan-sdk-demo-0.1.0-SNAPSHOT.jar
```

打开 `http://localhost:8080`，即可体验：

- 手机号与密码触发短信登录
- 短信验证码换取 Token
- 配置个人、家庭或私密空间参数
- 上传图片并获取直链预览

可通过环境变量启动：

```bash
WOPAN_ACCESS_TOKEN=xxx \
WOPAN_REFRESH_TOKEN=xxx \
java -jar wopan-sdk-demo/target/wopan-sdk-demo-0.1.0-SNAPSHOT.jar
```

## SDK 能力

- 短信登录与 Token 刷新
- 文件列表与空间用量查询
- 下载链接获取
- 新建目录、重命名与删除
- 个人空间、家庭空间、私密空间操作
- 文件分片上传与失败重试回调

更多示例见：

- [Java SDK 文档](wopan-sdk-java/README.md)
- [Spring Boot Starter 文档](wopan-spring-boot-starter/README.md)
- [Demo 文档](wopan-sdk-demo/README.md)

## 安全说明

- 不要将真实 `access-token`、`refresh-token` 或账号密码提交到代码仓库。
- 推荐通过环境变量、配置中心或密钥管理服务注入敏感配置。
- 同一账号重新登录网页端后，旧 Token 可能失效，需要重新获取或依赖自动刷新流程。
