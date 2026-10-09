# Halo 认证方式

目前 Halo 支持的认证方式有：

- 基本认证（Basic Auth）
- 表单登录（Form Login）

计划支持的认证方式有：

- [个人令牌认证（Personal Access Token）](https://github.com/halo-dev/halo/issues/1309)
- [OAuth2](https://oauth.net/2/)

## 基本认证

这是最简单的一种认证方式，通过简单设置 HTTP 请求头 `Authorization: Basic xxxyyyzzz==` 即可实现认证，访问 Halo API，例如：

```bash
╰─❯ curl -u "admin:P@88w0rd" -H "Accept: application/json" http://localhost:8090/api/v1alpha1/users

或者
╰─❯ echo -n "admin:P@88w0rd" | base64
YWRtaW46UEA4OHcwcmQ=
╰─❯ curl -H "Authorization: Basic YWRtaW46UEA4OHcwcmQ=" -H "Accept: application/json" http://localhost:8090/api/v1alpha1/users
```

## 表单认证

这是一种比较常用的认证方式，只需提供用户名和密码以及 `CSRF 令牌`（用于防止重复提交和跨站请求伪造）。

- 表单参数

  |    参数名     |   类型   |           说明            |
  |------------|--------|-------------------------|
  | username   | form   | 用户名                     |
  | password   | form   | 密码                      |
  | _csrf      | form   | `CSRF` 令牌。由客户端随机生成。     |
  | XSRF-TOKEN | cookie | 跨站请求伪造令牌，和 `_csrf` 的值一致 |

- HTTP 200 响应

  仅在请求头 `Accept` 中包含 `application/json` 时发生，响应示例如下所示：

  ```bash
  ╰─❯ curl 'http://localhost:8090/login' \
    -H 'Accept: application/json' \
    -H 'Cookie: XSRF-TOKEN=1ff67e0c-6f2c-4cf9-afb5-81bc1015b8e5' \
    -H 'Content-Type: application/x-www-form-urlencoded' \
    --data-raw '_csrf=1ff67e0c-6f2c-4cf9-afb5-81bc1015b8e5&username=admin&password=P@88w0rd'
  ```

  ```bash
  < HTTP/1.1 200 OK
  < Vary: Origin
  < Vary: Access-Control-Request-Method
  < Vary: Access-Control-Request-Headers
  < Content-Type: application/json
  < Content-Length: 161
  < Cache-Control: no-cache, no-store, max-age=0, must-revalidate
  < Pragma: no-cache
  < Expires: 0
  < X-Content-Type-Options: nosniff
  < X-Frame-Options: DENY
  < X-XSS-Protection: 1 ; mode=block
  < Referrer-Policy: no-referrer
  < Set-Cookie: SESSION=d04db9f7-d2a6-4b7c-9845-ef790eb4a980; Path=/; HttpOnly; SameSite=Lax
  ```

  ```json
  {
    "username": "admin",
    "authorities": [
      {
        "authority": "ROLE_super-role"
      }
    ],
    "accountNonExpired": true,
    "accountNonLocked": true,
    "credentialsNonExpired": true,
    "enabled": true
  }
  ```
- HTTP 302 响应

  仅在请求头 `Accept` 中不包含 `application/json`才会发生，响应示例如下所示：

  ```bash
  ╰─❯ curl 'http://localhost:8090/login' \
    -H 'Accept: */*' \
    -H 'Cookie: XSRF-TOKEN=1ff67e0c-6f2c-4cf9-afb5-81bc1015b8e5' \
    -H 'Content-Type: application/x-www-form-urlencoded' \
    --data-raw '_csrf=1ff67e0c-6f2c-4cf9-afb5-81bc1015b8e5&username=admin&password=P@88w0rd'
  ```

  ```bash
  < HTTP/1.1 302 Found
  < Vary: Origin
  < Vary: Access-Control-Request-Method
  < Vary: Access-Control-Request-Headers
  < Location: /console/
  < Cache-Control: no-cache, no-store, max-age=0, must-revalidate
  < Pragma: no-cache
  < Expires: 0
  < X-Content-Type-Options: nosniff
  < X-Frame-Options: DENY
  < X-XSS-Protection: 1 ; mode=block
  < Referrer-Policy: no-referrer
  < Set-Cookie: SESSION=9ce6ad3f-7eba-4de5-abca-650b4721c7ac; Path=/; HttpOnly; SameSite=Lax
  < content-length: 0
  ```

未来计划支持“记住我（Remember Me）”功能。

## Personal Access Token

### 背景

Halo 是一款现代化的开源 CMS / 建站系统，为了便于开发者和用户利用 API 访问网站数据，Halo 支持了 Personal Access Token（以下简称
PAT）功能。
用户可以在 Halo 的后台生成 PAT，它是一个随机字符串，用于在 API 请求头里提供验证身份用。Halo 后端在接收请求时会校验 PAT
的值，如果匹配就会允许访问相应的 API 数据。
这种 PAT 机制避免了直接使用用户名密码的安全隐患，开发者可以为每个 PAT 设置访问范围、过期时间等。同时使用随机 PAT
也增加了安全性。这为开发 Halo 插件和应用提供了更安全简便的认证方式。
相比直接暴露服务端 API，这种 PAT 机制也更标准化和安全可控。Halo 在参考业内主流做法的基础上，引入了 PAT，以便于生态系统的开放与丰富。

### 设计

PAT 以 `pat_` 开头，剩余部分为 `PAT 名称:随机密钥` 经过 Base64URL（无填充）编码后的字符串，例如：

```text
pat_cGF0LWFkbWluLWxvYmttOmdBWDBBMUp1SzgxVTg4dFdScFJDd05oRmNxTk12bkI2
```

其中随机密钥为 192 位随机数（Base64URL 编码后 32 个字符）。服务端仅保存密钥的 SHA-256
哈希值（存储在 `spec.tokenId` 中），校验时通过恒定时间比较进行匹配，因此完整的 PAT 只会在创建时返回一次。

需要注意的是，在此变更之前签发的 `pat_` + [JWT](https://datatracker.ietf.org/doc/html/rfc7519) 形式的 PAT
仍然可用，直到被撤销或过期。

### 使用方式

#### 生成 PAT

Halo 专门提供了生成 PAT 的端口：`/apis/api.console.security.halo.run/v1alpha1/users/-/personalaccesstokens`。创建 PAT
请求示例如下：

```shell
curl -u admin:admin -X 'POST' \
  'http://localhost:8090/apis/api.console.security.halo.run/v1alpha1/users/-/personalaccesstokens' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "spec": {
    "name": "My PAT",
    "description": "This is my first PAT.",
    "expiresAt": "2023-09-15T02:42:35.136Z"
    "roles": [""]
  }
}'
```

```json
{
  "spec": {
    "description": "This is my first PAT.",
    "expiresAt": "2023-09-16T02:42:35.136Z",
    "roles": [],
    "username": "admin",
    "revoked": false,
    "tokenId": "157a402e40e89d20c1005ad17ab52a156e15bafa16056cbd53d010d4d186f332"
  },
  "apiVersion": "security.halo.run/v1alpha1",
  "kind": "PersonalAccessToken",
  "metadata": {
    "generateName": "pat-admin-",
    "name": "pat-admin-lobkm",
    "annotations": {
      "security.halo.run/access-token": "pat_cGF0LWFkbWluLWxvYmttOmdBWDBBMUp1SzgxVTg4dFdScFJDd05oRmNxTk12bkI2"
    },
    "version": 0,
    "creationTimestamp": "2023-09-15T03:08:18.875350Z"
  }
}
```

请求体说明如下表所示：

|     属性名     |                                                 描述                                                 |
|-------------|----------------------------------------------------------------------------------------------------|
| name        | PAT 名称。必填。                                                                                         |
| description | PAT 描述。非必填。                                                                                        |
| expiresAt   | PAT 过期时间，一旦创建不可修改，或修改无效。如果不填写，则表示 PAT 无过期时间。                                                       |
| roles       | 授权给 PAT 的角色，必须包含在当前用户所拥有的角色内。如果设置为 `null` 或者 `[]`，则表示当前 PAT 仅会拥有 `anonymous` 和 `authenticated` 角色。 |

响应体说明如下所示：

|              属性路径              |                      描述                      |
|--------------------------------|----------------------------------------------|
| security.halo.run/access-token | 生成好的 PAT。需要注意的是，这个 PAT 不会保存在数据库中，所以仅有一次保存机会。 |

#### 使用 PAT

向 Halo 发送请求时，携带 Header：`Authorization: Bearer $PAT` 即可。示例如下：

```shell
curl http://localhost:8090/apis/api.console.halo.run/v1alpha1/users/- \
  -H "Authorization: Bearer pat_cGF0LWFkbWluLWxvYmttOmdBWDBBMUp1SzgxVTg4dFdScFJDd05oRmNxTk12bkI2"
```

