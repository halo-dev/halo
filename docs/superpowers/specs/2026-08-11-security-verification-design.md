# 安全验证（Security Verification）设计

日期：2026-08-11

## 背景与问题

当前 UC（个人中心）修改密码仅验证原密码（有密码时），不验证邮箱验证码或 TOTP。攻击者一旦拿到密码即可改密并将用户锁在门外。期望在修改密码前先通过安全验证（邮箱验证码或 TOTP）。

## 目标与非目标

目标：
- 修改密码前必须通过安全验证（邮箱验证码 **或** TOTP），并保留原密码验证。
- 验证页用**主题模板**实现（与登录、二步验证登录页同机制），主题可覆盖。
- 验证为**通用安全验证**（GitHub sudo 模式）：验证通过后进入一个有时效的会话"已验证"窗口，后续其他敏感操作（改邮箱、删账号、绑手机号等）可复用该窗口作为前置条件。

非目标（本次不实现）：
- 手机号验证（商业版后续集成，社区版不做）。
- 除修改密码外的其他敏感操作接入该验证。
- 邮箱绑定流程改动。

## 已确认的设计决策

1. 仅**已有密码**的用户需要安全验证；passwordless 用户首次设置密码不要求验证（会话本身已通过 OAuth2/邮箱登录强认证，且无密码可被盗）。
2. 已有密码但**既无已验证邮箱也无 TOTP** 的用户（早期注册用户）回退到仅原密码验证，不被卡死。
3. 两种方式都可用时，**用户任选其一**（邮箱验证码或 TOTP 码），任一通过即可。
4. 验证通过后会话标记 **30 分钟**有效（常量），有效期内直接改密，不重复验证。
5. 验证页为服务端渲染的 Thymeleaf 页面，**不新增 Vue 页面**。

## 用户流程

1. Profile 页点击"修改密码" → `GetMyUser` 返回 `passwordChangeVerificationRequired=true` → 整页跳转 `/security-verification?redirect=/uc/profile?password-change=1`。
2. 服务端渲染验证页，只展示当前用户可用的验证方式（邮箱验证码 + 发送按钮/倒计时，或 TOTP 码）。两种都可用时，表单顶部渲染**分段切换器**（tab："邮箱验证码" / "TOTP 验证码"），默认选邮箱验证码（邮箱不可用则默认 TOTP），vanilla JS 切换可见表单块。
3. 用户提交（JS fetch + CSRF header）→ 后端校验 → 写 WebSession 标记 → 302 回 `redirect`；失败 → `?error=invalid-code`。
4. 回到 `/uc/profile`，页面检测到 `?password-change=1` 且 `passwordChangeVerificationRequired` 已为 false → **自动打开**改密弹窗。
5. 提交改密（请求体不变：原密码 + 新密码）；30 分钟内免再次验证。

## 后端设计

### 页面端点：`SecurityVerificationEndpoint`

POST-auth 页面端点（仿 `PreAuthTwoFactorEndpoint` 渲染 + `TwoFactorAuthSecurityConfigurer` 校验的组合）：

|                  方法/路径                   |                                                               行为                                                               |
|------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| `GET /security-verification`             | 渲染 `security-verification` 页面模板，注入：可用方式（emailVerified / totpConfigured）、redirect 参数。未认证 → 302 登录；无可用方式 → 302 回 redirect（不弹验证页） |
| `POST /security-verification/email-code` | 向已验证邮箱发送 6 位验证码（JSON 响应，错误信息给前端 JS 展示）                                                                                         |
| `POST /security-verification`            | 校验 `emailCode` 或 `totpCode`（任一通过即可；TOTP 已配置则校验 TOTP，否则校验邮箱码，都未提供 → 400）→ 写 session 标记 → 302 到 redirect                         |

安全要求：
- **redirect 仅接受站内相对路径**（以 `/` 开头且非 `//`、非协议相对），防 open redirect；非法值 → 400 或默认跳 `/uc/profile`。
- 未认证访问 → 302 `/login`。

### 会话标记（通用 sudo 窗口）

- WebSession 属性：`security-verification.verified-at`，值 `Instant`。
- TTL 常量 30 分钟（`SecurityVerificationService` 内定义）。
- 标记随会话存在；改密成功后由现有 `PasswordChangedEvent` 机制注销其他会话，行为不变。

### 共享组件：`SecurityVerificationService`

`UcUserEndpoint`（改密守卫）与验证端点共用：

- `boolean isVerified(WebSession)` — 标记存在且未过期。
- `void markVerified(WebSession)` — 写入标记。
- `boolean isAvailable(User)` — `emailVerified || totpConfigured`。

### 服务与复用

- `EmailVerificationService` 新增两个方法（impl 复用现有 `EmailVerificationManager` 内存码机制：10 分钟过期、5 次尝试、黑名单）：
  - `sendSecurityVerificationCode(username)` — 校验邮箱已验证后发码到 `spec.email`。
  - `verifySecurityVerificationCode(username, code)` — 一次性校验，**不改变邮箱绑定**（现有 `verify()` 是绑定语义，不可复用）。
- 新增 ReasonType `security-verification` + NotificationTemplate（"安全验证"邮件，标题如「安全验证-[site.title]」）——`notification.yaml` / `notification-templates.yaml` 各加一段，复用现有通知机制。
- TOTP 校验逻辑从 `TwoFactorAuthEndpoint` 私有方法抽为共享 `TotpVerificationService`，`TwoFactorAuthEndpoint` 与验证端点共用。

### 授权与改密守卫

- `authenticatedAuthorizationConfigurer`（Order 300）新增 `/security-verification/**` → authenticated。
- `UcUserVo` 新增 `passwordChangeVerificationRequired`，服务端计算：`passwordSet && isAvailable(user) && !isVerified(session)`。
- `changeMyPassword` 增加兜底守卫：需验证但会话无有效标记 → 403 + problemDetail（`user.password.verification.required`，携带验证页 URL，仿 `TwoFactorAuthRequiredException` 模式），前端可据此跳转。

### 模板文件（`application/src/main/resources/templates/`，主题可覆盖）

- `security-verification.html` — 页面模板（`gateway_fragments/layout` 布局，仿 `challenges/two-factor/totp.html`）。
- `gateway_fragments/security-verification.html` — 表单 fragment（JS 参照 `login_email-code.html`：fetch + `_csrf.headerName`/`_csrf.token` + 发送倒计时）：
  - 两种方式都可用时：顶部分段切换器（tab）切换「邮箱验证码」/「TOTP 验证码」，vanilla JS 切换可见表单块；两种表单块的输入分别命名为 `emailCode` / `totpCode`，未激活块输入置 `disabled`（disabled 输入不随表单提交，服务端读哪个字段有值即知道用的是哪种方式，无需额外 method 字段同步）。
  - 邮箱验证码块：验证码输入 + "发送验证码"按钮（倒计时，仿 `login_email-code`）。
  - TOTP 块：6 位数字输入，输满 6 位自动提交（仿 `totp.html` 的 `form.requestSubmit()` 模式）。
  - 共享底部"验证"提交按钮。
- `security-verification.properties`（default / en / es / zh_TW，跟随现有模式）。

## 前端设计

- 仅 `Profile.vue` 接线：点击"修改密码"时检查 `passwordChangeVerificationRequired` → 是则整页跳转验证页；返回时若 query 带 `password-change=1` 且标记已生效（重新拉取 `GetMyUser` 后字段为 false）→ 自动打开改密弹窗。
- `PasswordChangeModal.vue` 不变（仍只提交 `oldPassword` + `password`）。

## 边界情况

|        场景         |             行为             |
|-------------------|----------------------------|
| 无密码用户首次设置密码       | 不要求验证                      |
| 有密码但无已验证邮箱且无 TOTP | 回退仅原密码，不跳验证页               |
| 两种方式都可用           | 页面任选其一                     |
| 会话标记未过期           | 直接改密，不重复验证                 |
| 匿名访问验证页           | 302 登录                     |
| redirect 为外部 URL  | 拒绝（400 / 默认 `/uc/profile`） |
| 验证码 / TOTP 均未提供   | 400                        |

## 测试计划

- `SecurityVerificationEndpoint` 集成测试：页面渲染、发码、验证成功写标记 + 302、错误码、未认证重定向、open redirect 拒绝、无可用方式用户直接回跳。
- `UcUserEndpoint`：标记生效 / 过期 / 无能力回退 / 403 守卫。
- `EmailVerificationServiceImpl` 新方法测试（发码前置条件、一次性校验、不改变绑定）。
- `TotpVerificationService` 抽取后沿用现有 2FA 测试。

## 命名总览

|    项    |                                   名称                                   |
|---------|------------------------------------------------------------------------|
| 页面路径    | `/security-verification`                                               |
| 页面端点    | `SecurityVerificationEndpoint`                                         |
| 会话标记    | `security-verification.verified-at`（Instant，TTL 30 分钟）                 |
| 共享组件    | `SecurityVerificationService`（isVerified / markVerified / isAvailable） |
| 异常      | `SecurityVerificationRequiredException`                                |
| 邮箱服务方法  | `sendSecurityVerificationCode` / `verifySecurityVerificationCode`      |
| 邮件原因类型  | `security-verification`                                                |
| UC 接口字段 | `UcUserVo.passwordChangeVerificationRequired`（按操作命名，由通用检查派生）           |

## 契约变更

`./gradlew generateOpenApiDocs && pnpm -C ui api-client:gen` 重新生成 UI 客户端。
