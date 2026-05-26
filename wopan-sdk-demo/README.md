# WoPan SDK Demo

这个 demo 是一个 Spring Boot + Java 17 Web 项目，已引入 `wopan-spring-boot-starter`，用来演示如何配置 `WoPanClient`，上传图片，并通过 SDK 返回的 `fid` 获取下载直链做页面预览。

## 运行

在工作区根目录执行：

```bash
mvn -q -pl wopan-sdk-demo -am package
java -jar wopan-sdk-demo/target/wopan-sdk-demo-0.1.0-SNAPSHOT.jar
```

启动后打开：

```text
http://localhost:8080
```

## Spring Boot 配置

配置文件在：

```text
wopan-sdk-demo/src/main/resources/application.yml
```

示例：

```yaml
wopan:
  access-token: your_access_token
  refresh-token: your_refresh_token
  auto-refresh: true
  parent-id: "0"
  space-type: PERSONAL
  family-id: ""
  private-space-password: ""
  base-url: ""
  zone-url: ""
```

这些配置会绑定到 `WoPanProperties`，启动时初始化到 demo 的运行时配置中；页面上保存配置时，只会更新内存里的运行时配置，不会回写 `application.yml`。

## 配置项说明

- `access-token`：沃盘登录后的 `access_token`，SDK 调接口必须带它。可以通过 SDK 的短信登录流程、浏览器抓取登录态，或已有系统保存的 token 获取。
- `refresh-token`：沃盘登录后的 `refresh_token`，用于 token 过期后刷新。来源和 `access-token` 一样，通常和 access token 一起返回。
- `auto-refresh`：是否允许 SDK 在接口返回 token 失效时自动调用刷新接口。一般保持 `true`。
- `parent-id`：图片上传目标目录 ID。根目录通常是 `0`；子目录 ID 可以通过 SDK 的 `listFiles` 查目录列表获取。
- `space-type`：上传空间类型，支持 `PERSONAL` 个人空间、`FAMILY` 家庭空间、`PRIVATE` 私密空间。
- `family-id`：家庭空间 ID。只有 `space-type=FAMILY` 时需要；不填时 SDK 会尝试获取默认家庭 ID。
- `private-space-password`：私密空间密码。只有 `space-type=PRIVATE` 时需要，SDK 会用它换取 `psToken`。
- `base-url`：沃盘 dispatcher API 基础地址。正常不用填，除非你要走测试环境、代理或覆盖默认域名。
- `zone-url`：上传分区地址。正常不用填，SDK 会自动通过 `getZoneInfo` 获取；只有你想固定上传域名时才配置。

也可以用环境变量覆盖配置文件：

```bash
WOPAN_ACCESS_TOKEN=xxx \
WOPAN_REFRESH_TOKEN=xxx \
WOPAN_PARENT_ID=0 \
WOPAN_SPACE_TYPE=PERSONAL \
java -jar wopan-sdk-demo/target/wopan-sdk-demo-0.1.0-SNAPSHOT.jar
```

可选环境变量：

- `WOPAN_DEMO_PORT`：服务端口，默认 `8080`
- `WOPAN_ACCESS_TOKEN` / `WOPAN_ACCESS`：SDK access token
- `WOPAN_REFRESH_TOKEN` / `WOPAN_REFRESH`：SDK refresh token
- `WOPAN_PARENT_ID`：上传目录 ID，默认 `0`
- `WOPAN_SPACE_TYPE`：`PERSONAL`、`FAMILY` 或 `PRIVATE`
- `WOPAN_FAMILY_ID`：家庭空间 ID
- `WOPAN_PRIVATE_SPACE_PASSWORD`：私密空间密码
- `WOPAN_BASE_URL`：覆盖 SDK baseUrl
- `WOPAN_ZONE_URL`：覆盖 SDK zoneUrl

## 功能

- 手机号 + 密码触发沃盘短信登录
- 短信验证码确认登录并获取 `access_token` / `refresh_token`
- 登录成功后自动把 token 写入当前 demo SDK 配置
- 页面运行时保存 SDK 配置
- 支持个人空间、家庭空间、私密空间上传参数
- 图片本地预览
- 调用 `uploadFile` 上传图片
- 调用 `getDownloadLinks` 获取直链并展示网盘预览

## 登录获取 Token

页面里的“手机号登录获取 Token”对应 SDK 调用：

```java
WoPanClient client = WoPanClient.builder().build();
client.loginWithPassword(phone, password);
TokenPair tokenPair = client.confirmLoginWithSms(phone, password, smsCode);
```

接口对应关系：

- `POST /api/login/start`：传 `phone`、`password`，调用 `loginWithPassword`，触发短信验证码。
- `POST /api/login/confirm`：传 `phone`、`password`、`smsCode`，调用 `confirmLoginWithSms`，返回并写入 token。

登录成功后页面会把 `accessToken` 和 `refreshToken` 自动填入配置表单，并更新 demo 内存配置。为了避免意外泄漏，后端不会把密码写入配置文件或日志。
