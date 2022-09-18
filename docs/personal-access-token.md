# Personal Access Token Design

私人访问令牌（Personal Access Token，以下简称 PAT）主要用于第三方系统集成使用。比如，开发 VSCode 插件用于写作文章、上传附件、发布日志等。

## Requirements

- 用户能够创建 PAT，但只能在首次创建的时候拿到 token。
- 用户创建 PAT 时能够选择合适的 Scope。
- 用户创建 PAT 时能选择过期时间。
- 系统应提前创建好所需的 Scope。
- 插件应能扩展需要的 Scope。
- PAT 用户间相互隔离。
- 用户能够利用 Token 通过 Basic Auth 方式访问接口。
- 用户能重新生成 Token。
- 用户能更新 PAT，如果需要更新过期时间，则需要重新生成 Token。

## PAT Extension 样例

```yaml
apiVersion: security.halo.run/v1alpha1
kind: PersonalAccessToken
spec:
  displayName: For VSCode Plugin # 展示名称
  createdBy:
    name: johnniang # PAT 创建者
  expiresAt: 2022.09.09T11:11:11Z # 过期时间
  revoked: false # 当前令牌是否被撤销
  scopes: # 选择的访问范围
    - post.read
    - post.write
    - profile
  encodedToken: $2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW # BCrypt 加密后的结果
```

## Scope Extension 样例

```yaml
apiVersion: v1alpha1
kind: ConfigMap
metadata:
  name: scopes
data:
  scopes: |
    [{}, {}]

---
- name: post:write
  description: Full control of posts, categories and tags.
  include:
  - name: published_post:read
    description: Read published posts, categories and tags.

- name: attachment:write
  description: Full control of attachments
  include:
  - name: attachment:read
    description: Read all attachments

- name: theme:write
  description: Full control of themes
  include:
  - name: theme:read
    description: Read all themes

- name: menu:write
  description: Full control of menus and menu items
  include:
  - name: menu:read
    description: Read all menus and menu items

- name: user:write
  description: Update all user data
  include:
  - name: user:read
    description: Read all user profile data
  - name: user:email:read
    description: Access user email address(read only)

- name: plugin-a:extension-b:write
  description: Full control of extenion-b provided by plugin-a
  include:
  - name: plugin-a:extension-b:read
    description: Read all extension-bs
```

```yaml
apiVersion: security.halo.run/v1alpha1
kind: Scope
metadata:
  name: public-post-read
spec:
  description: Read public posts
  identity: public_post:read # 如果插件需要扩展 scope，一定得加上插件 ID 前缀，例如：`plugin-a:`。
  include:
    - name: xxx
  dependOn:
    - name: yyy
```

## PAT 工作时序图

## PAT 解密后的构成

Decrypt PAT: decrypt(pat) using secret to (username & random_text)
Encrypt PAT: encrypt(usernae & random_text) using secret.

生成 PAT 的时候需要将 `user_name:pat_name:expiration:random_string` 加密