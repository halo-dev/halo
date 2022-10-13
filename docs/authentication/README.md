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

    | 参数名     | 类型   | 说明                                  |
    | ---------- | ------ | ------------------------------------- |
    | username   | form   | 用户名                                |
    | password   | form   | 密码                                  |
    | _csrf      | form   | `CSRF` 令牌。由客户端随机生成。       |
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
