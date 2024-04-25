# 插件中如何实现 WebSocket

## 背景

> https://github.com/halo-dev/halo/issues/5285

越来越多的开发者在开发插件过程中需要及时高效获取某些资源的最新状态，但是因为在插件中不支持 WebSocket，故只能选择定时轮训的方式来解决。

在插件中支持 WebSocket 的功能需要 Halo Core 来适配并制定规则以方便插件实现 WebSocket。

## 实现

插件中实现 WebSocket 的代码样例如下所示：

```java
@Component
public class MyWebSocketEndpoint implements WebSocketEndpoint {

  @Override
  public GroupVersion groupVersion() {
    return GroupVersion.parseApiVersion("my-plugin.halowrite.com/v1alpha1");
  }

  @Override
  public String urlPath() {
    return "/resources";
  }

  @Override
  public WebSocketHandler handler() {
    return session -> {
      var messages = session.receive()
              .map(message -> {
                var payload = message.getPayloadAsText();
                return session.textMessage(payload.toUpperCase());
              });
      return session.send(messages);
    };
  }
}
```

插件安装成功后，可以通过 `/apis/my-plugin.halowrite.com/v1alpha1/resources` 进行访问。 示例如下所示：

```bash
websocat --basic-auth admin:admin ws://127.0.0.1:8090/apis/my-plugin.halowrite.com/v1alpha1/resources
```

同样地，WebSocket 相关的 API 仍然受当前权限系统管理。
