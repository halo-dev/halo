# 插件中如何发送共享事件（SharedEvent）

在插件中，可以通过共享事件（SharedEvent）来发送消息。 共享事件是一种特殊的事件，它可以被核心和所有插件订阅。

## 订阅共享事件

目前，核心中已经提供了不少的共享事件，例如 `run.halo.app.event.post.PostPublishedEvent`、`run.halo.app.event.post.PostUpdatedEvent`
，这些事件由核心发布，核心和插件均可订阅。请看下面的示例：

```java

@Component
public class PostPublishedEventListener implements ApplicationListener<PostPublishedEvent> {

  @Override
  public void onApplicationEvent(PostPublishedEvent event) {
    // Do something
  }

}
```

或者通过 `@EventListener` 注解实现，

```java

@Component
public class PostPublishedEventListener {

  @EventListener
  // @Async // 如果需要异步处理，可以添加此注解
  public void onPostPublished(PostPublishedEvent event) {
    // Do something
  }

}
```

> 需要注意的是，只有被 `@SharedEvent` 注解标记的事件才能够被其他插件或者核心订阅。

## 发送共享事件

在插件中，我们可以通过 `ApplicationEventPublisher` 来发送共享事件，请看下面的示例：

```java

@Service
public class PostService {

  private final ApplicationEventPublisher eventPublisher;

  public PostService(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  public void publishPost(Post post) {
    // Do something
    eventPublisher.publishEvent(new PostPublishedEvent(post));
  }

}
```

## 创建共享事件

在插件中，我们可以创建自定义的共享事件，供其他插件订阅，示例如下：

```java

@SharedEvent
public class MySharedEvent extends ApplicationEvent {

  public MySharedEvent(Object source) {
    super(source);
  }

}
```

> 需要注意的是：
> 1. 共享事件必须继承 `ApplicationEvent`。
> 2. 共享事件必须被 `@SharedEvent` 注解标记。
> 3. 如果想要被其他插件订阅，则需要将该事件类发布到 Maven 仓库中，供其他插件引用。
