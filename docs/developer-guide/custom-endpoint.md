# 系统自定义 API

系统自定义 API 是一组特殊的 API，因为自定义模型 API 无法满足要求，需要开发者自己实现。

但是系统自定义 API 有一个统一的前缀：`/apis/api.console.halo.run/v1alpha1/`，剩余的部分可随意定义。

## 如何在系统中创建一个系统自定义 API

1. 实现 `run.halo.app.core.extension.endpoint.CustomEndpoint` 接口
2. 将实现类设置为 Spring Bean

关于用户的自定义 API 实现类如下：

```java

@Component
public class UserEndpoint implements CustomEndpoint {

    private final ExtensionClient client;

    public UserEndpoint(ExtensionClient client) {
        this.client = client;
    }

    Mono<ServerResponse> me(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> {
                    var name = ctx.getAuthentication().getName();
                    return client.fetch(User.class, name)
                            .orElseThrow(() -> new ExtensionNotFoundException(name));
                })
                .flatMap(user -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(user));
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return SpringdocRouteBuilder.route()
                .GET("/users/-", this::me, builder -> builder.operationId("GetCurrentUserDetail")
                        .description("Get current user detail")
                        .tag("api.console.halo.run/v1alpha1/User")
                        .response(responseBuilder().implementation(User.class)))
                // 这里可添加其他自定义 API
                .build();
    }
}
```

这样我们就可以启动 Halo，访问 Swagger UI 文档地址，并进行测试。
