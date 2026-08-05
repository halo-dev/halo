# OAuth2 登录后注册新用户与邮箱补充门禁设计

日期：2026-08-04

## 背景与目标

当前 OAuth2 登录流程：OAuth2 身份从未绑定且用户未登录时，系统把 OAuth2 身份缓存到 Session，并跳转到 `/login?oauth2_bind`，提示用户先通过其他方式登录，登录成功后自动绑定。

目标：

1. OAuth2 登录未绑定时，先进入一个选择页，用户可以选择“绑定已有用户”或“注册新用户”。
2. “注册新用户”根据 OAuth2/OIDC 身份自动创建 Halo 用户，并绑定 OAuth2 连接。
3. 注册时如果拿不到可用邮箱，用户仍然创建成功，但后续请求（除登出外）会被引导到补邮箱页 `/complete-profile`。
4. 补邮箱页是否要求验证码由“注册需验证邮箱”设置决定，验证码可选时用户仍可主动验证。

## 现状

- `MapOAuth2AuthenticationFilter`：OAuth2 回调后，已存在连接则直接登录；当前已登录则自动绑定；两者皆无则缓存 OAuth2 身份、登出并重定向 `/login?oauth2_bind`。
- `/login?oauth2_bind`：网关登录页展示提示，用户登录成功后由过滤器自动创建 `UserConnection` 完成绑定。
- `UserConnectionService`：负责创建/更新/删除 OAuth2 连接。
- `SystemSetting.User`：`allowRegistration`（开放注册）、`mustVerifyEmailOnRegistration`（注册需验证邮箱）、`defaultRole`、`protectedUsernames`、`requiredAgreementPages`。
- `EmailVerificationService`：基于用户名的验证码发送与校验（现有控制台接口会校验密码，不适合 OAuth2 用户）。
- `HaloServerRequestCache`：保存登录前请求，登录/注册后可回到原目标。

## 已确认的产品决策

### 选择页

- 选择页为网关服务端页面，URL 为 `/login?oauth2_select`。
- “绑定已有用户”跳转 `/login?oauth2_bind`，继续现有绑定逻辑。
- “注册新用户”提交到 `POST /login/oauth2/register`：
  - “开放注册”关闭时按钮禁用，后端仍然强制校验。
  - 配置了“注册时所需协议”时，选择页展示协议勾选框，必须勾选才能注册。

### 注册规则

- 用户名候选：依次取 `login`、`username`、`user_name`、OIDC `preferred_username`、`nickname` 属性，全部缺失时才回退到 `OAuth2User.getName()`（OIDC 即 `sub`），小写化后按现有用户名规范（`NAME_REGEX`、4–63 位）与保留名单、占用情况校验；不满足则生成随机用户名（`user-` + 8 位小写字母数字，最多重试 20 次）。
- 显示名：`name` → `nickname`/`display_name` → OIDC `preferred_username` → 候选用户名；命中显示名保留名单则退化为候选用户名。
- 邮箱：
  - OIDC 使用 `OidcUser.getEmail()`；普通 OAuth2 使用 `email` 属性。
  - 邮箱需小写化、格式合法，且未被其他“已验证”用户占用（沿用现有语义：只有已验证邮箱算占用）。
  - 普通 OAuth2：邮箱可用 → 写入并 `emailVerified=true`。
  - OIDC `email_verified=true` → 写入并已验证；`false` 或缺失 → 写入但未验证。
  - 邮箱缺失、格式不合法或已被占用 → 不写入，保持空邮箱。
- 密码：不设置，OAuth2 用户无密码；补邮箱相关接口均不校验密码。
- 角色：使用 `defaultRole`。

### 门禁规则

- 仅当“注册需验证邮箱”开启时启用门禁。
- 开启时，已登录用户 `spec.emailVerified == false`（空邮箱视为未验证）即被拦截。
- “注册需验证邮箱”关闭时不拦截任何人，即使邮箱为空。
- 超级管理员（authorities 含 `ROLE_super-role`）始终豁免。
- HTML 请求：302 到 `/complete-profile`，原目标存入请求缓存。
- 非页面请求（API/XHR）：403 ProblemDetails，固定 `type = email-not-set`。本期不做前端拦截器。

### 补邮箱页

- 网关服务端页面，URL `/complete-profile`。
- 提交邮箱：
  - “注册需验证邮箱”开启：验证码必填，验证通过后 `emailVerified=true`。
  - “注册需验证邮箱”关闭：验证码可选，填写且通过则标记已验证，不填则仅保存邮箱（此时无门禁，直接放行）。
  - 邮箱与其他已验证用户重复 → 表单报错。
- 验证码发送/校验复用 `EmailVerificationService`（按用户名，不校验密码），沿用限流与邮件模板。
- 成功后可回到请求缓存中的原目标（默认 `/uc`）。

## 设计

### 整体流程

1. 用户点击 OAuth2 登录 → 回调 → `MapOAuth2AuthenticationFilter`：
   - 已有连接 → 直接登录（不变）。
   - 当前已登录 → 自动绑定（不变）。
   - 无连接且未登录 → 缓存 OAuth2/OIDC 身份到 Session，重定向改为 `/login?oauth2_select`。
2. 选择页：绑定 → `/login?oauth2_bind`；注册 → `POST /login/oauth2/register`。
3. 注册端点：读取 Session 缓存身份 → 校验开放注册与协议 → 创建用户、默认角色、`UserConnection` → 写入登录态 → 清理缓存 → 跳转（命中门禁则 `/complete-profile`，否则回原目标）。
4. 门禁过滤器：按门禁规则拦截已认证请求。
5. `/complete-profile`：填写/验证邮箱，完成后回原目标。

### 组件

|               组件               |                             职责                              |
|--------------------------------|-------------------------------------------------------------|
| `OAuth2RegistrationService`（新） | 注册核心逻辑：用户名/显示名/邮箱推导、随机兜底、幂等、创建用户与连接。可独立单元测试。                |
| 预认证端点（新，`/login` 下）            | 渲染 `/login?oauth2_select`；处理 `POST /login/oauth2/register`。 |
| `EmailCompletionFilter`（新）     | 门禁 WebFilter，插在认证过滤器之后。                                     |
| 补邮箱端点（新，`/complete-profile` 下） | 渲染页面、发送验证码、提交邮箱与验证码。                                        |
| 网关模板（新）                        | `oauth2_select` 片段、`complete_profile` 页面。                   |
| 前端                             | 本期无逻辑改动；重新生成 api-client（`User.email` 变为可选）。                 |

### 选择页渲染

`GET /login?oauth2_select` 需要提供模型：

- Session 缓存中 OAuth2 身份对应的登录方式名称与头像（通过 `registrationId` 查询 `AuthProvider`）。
- `allowRegistration`。
- `requiredAgreementPages`（配置了才显示勾选框）。
- 错误提示参数。

Session 中没有缓存身份时，重定向回 `/login`。

### 注册端点

`POST /login/oauth2/register`：

1. 从 Session 缓存读取 `OAuth2AuthenticationToken`，缺失则重定向 `/login`。
2. 校验 `allowRegistration`；校验协议勾选（配置了协议页时）；校验 `defaultRole` 已配置。
3. 幂等检查：按 `registrationId + providerUserId` 查询 `UserConnection`，已存在则跳过创建，直接进入登录步骤。
4. 按注册规则推导用户名、显示名、邮箱并创建用户。
5. 创建 `UserConnection`；若创建失败（如并发已绑定），补偿删除刚创建的用户并报错。
6. 通过 `userDetailsService.findByUsername` 构建 `HaloOAuth2AuthenticationToken`，保存安全上下文。
7. 调用 `loginHandlerEnhancer.onLoginSuccess`（沿用现有逻辑：更新连接、清理 Session 缓存、设备记录、登录处理）。
8. 跳转：命中门禁 → `/complete-profile`；否则请求缓存中的原目标（默认 `/uc`）。

### 门禁过滤器

- 以 `SecurityConfigurer` 方式加入，插在认证过滤器之后、授权过滤器之前。
- 未认证请求放行。
- 豁免路径：`/oauth2/**`、`/login/**`、`/signup`、`/password-reset/**`、`/logout`、`/complete-profile/**`、`/system/setup`、`/error`、静态资源。
- 超级管理员放行。
- 读取 `SystemSetting.User.mustVerifyEmailOnRegistration`：关闭则放行。
- 开启时读取当前用户，`emailVerified == false`：
  - HTML 请求：302 `/complete-profile`，原目标存入 `HaloServerRequestCache`。
  - 其他请求：403 ProblemDetails，`type = email-not-set`。

### 补邮箱端点

- `GET /complete-profile`：渲染页面，模型包含当前用户邮箱（预填）、`mustVerifyEmailOnRegistration`。
- `POST /complete-profile/send-email-code`（JSON）：校验邮箱格式与占用后调用 `EmailVerificationService.sendVerificationCode(username, email)`，沿用限流。
- `POST /complete-profile`（表单 + CSRF）：
  - 校验邮箱格式与占用。
  - 开启验证：校验验证码（`EmailVerificationService.verify(username, code)`），成功置 `emailVerified=true`。
  - 关闭验证：验证码可选，提供且通过则置 `emailVerified=true`，否则仅保存邮箱。
  - 成功后 302 到请求缓存原目标（默认 `/uc`）。
- 授权配置：`/complete-profile/**` 仅允许已认证用户；门禁过滤器对其豁免。

## 边界与错误处理

- 开放注册关闭、协议未勾选、默认角色缺失 → 选择页重渲染错误，不创建任何数据。
- 随机用户名重试 20 次仍冲突 → 报错，不落库部分数据。
- 连接已存在（重复提交/并发）→ 直接登录，不重复创建用户。
- 用户创建成功但连接创建失败 → 补偿删除用户并报错。
- 注册成功后写入的是 `HaloOAuth2AuthenticationToken`（不是 `OAuth2AuthenticationToken`），`MapOAuth2AuthenticationFilter` 不会二次映射。
- 补邮箱页发送验证码失败/限流/邮箱占用 → 复用现有异常与表单错误展示。

## 兼容性与公开 API 变化

- `api/`：`User.UserSpec.email` 从 `@Schema(requiredMode = REQUIRED)` 改为可选，否则空邮箱用户落库会被 JSON Schema 校验拒绝。该变更会体现在 OpenAPI 与 api-client 模型中。
- 控制台“创建用户”接口自身保留“邮箱必填”的业务校验，后台建用户行为不变。
- 不需要数据库迁移（email 字段已存在且允许空值）。
- 无新依赖。
- 需要重新生成 OpenAPI 文档与 api-client。

## 测试

- 单元：
  - `OAuth2RegistrationService`：用户名规范化与随机兜底、显示名回退、邮箱占用、OIDC `email_verified` 分支、幂等。
  - 随机用户名生成。
- 集成：
  - 注册端点：成功创建用户+连接+登录态；开放注册关闭拒绝；协议必选；token 清理。
  - 门禁过滤器：开关开/关、空邮箱/未验证/已验证、超级管理员豁免、HTML 302、JSON 403 `type`、豁免路径。
  - `/complete-profile`：必填/可选验证码、邮箱占用报错、发送验证码限流。
  - 空邮箱用户落库。

## 范围外（后续）

- 前端拦截器消费 `email-not-set`（本期仅后端拦截）。
- 手机号补充：页面 URL 与命名已为“完善资料”预留，后续可扩展字段或分步提交。

