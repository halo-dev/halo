# @halo-dev/api-client

Halo 2.0 的 JavaScript API 客户端请求库。使用 [OpenAPI Generator](https://openapi-generator.tech/) 生成。

## 使用

```javascript
import {
  coreApiClient,
  consoleApiClient,
  ucApiClient,
  publicApiClient,
  createCoreApiClient,
  createConsoleApiClient,
  createUcApiClient,
  createPublicApiClient,
  axiosInstance
} from "@halo-dev/api-client"
```

- **coreApiClient**: 为 Halo 所有自定义模型的 CRUD 接口封装的 api client。
- **consoleApiClient**: 为 Halo 针对 Console 提供的接口封装的 api client。
- **ucApiClient**: 为 Halo 针对 UC 提供的接口封装的 api client。
- **publicApiClient**: 为 Halo 所有公开访问的接口封装的 api client。
- **createCoreApiClient**: 用于创建自定义模型的 CRUD 接口封装的 api client，需要传入 axios 实例。
- **createConsoleApiClient**: 用于创建 Console 接口封装的 api client，需要传入 axios 实例。
- **createUcApiClient**: 用于创建 UC 接口封装的 api client，需要传入 axios 实例。
- **createPublicApiClient**: 用于创建公开访问接口封装的 api client，需要传入 axios 实例。
- **axiosInstance**: 内部默认创建的 axios 实例。

### 在插件中使用

```shell
pnpm install @halo-dev/api-client axios
```

由于已经在 Console 和 UC 项目中引入并设置好了 Axios 拦截器，所以直接使用即可：

```javascript
import { coreApiClient } from "@halo-dev/api-client"

coreApiClient.content.post.listPost().then(response => {
  // handle response
})
```

此外，在最新的 `@halo-dev/ui-plugin-bundler-kit@2.17.0` 中，已经排除了 `@halo-dev/api-client`、`axios` 依赖，所以最终产物中的相关依赖会自动使用 Halo 本身提供的依赖，无需关心最终产物大小。

详细文档可查阅：[插件开发 / API 请求](https://docs.halo.run/developer-guide/plugin/api-reference/ui/api-request)

### 在外部项目中使用

```shell
pnpm install @halo-dev/api-client axios
```

```javascript
import axios from "axios"

const axiosInstance = axios.create({
  baseURL: "http://localhost:8090"
})

const coreApiClient = createCoreApiClient(axiosInstance)

coreApiClient.content.post.listPost().then(response => {
  // handle response
})
```
