# Halo 认证扩展点

此前，Halo 提供了 AdditionalWebFilter 作为扩展点供插件扩展认证相关的功能。但是近期我们明确了 AdditionalWebFilter
的使用用途，故不再作为认证的扩展点。

目前，Halo 提供了三种认证扩展点：表单登录认证、普通认证和匿名认证。

## 表单登录（FormLogin）

示例如下：

```java
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.security.FormLoginSecurityWebFilter;

@Component
public class MyFormLoginSecurityWebFilter implements FormLoginSecurityWebFilter {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    // Do your logic here
    return chain.filter(exchange);
  }
}

```

## 普通认证（Authentication）

示例如下：

```java
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.security.AuthenticationSecurityWebFilter;

@Component
public class MyAuthenticationSecurityWebFilter implements AuthenticationSecurityWebFilter {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    // Do your logic here
    return chain.filter(exchange);
  }
}
```

## 匿名认证（Anonymous Authentication

示例如下：

```java
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.security.AnonymousAuthenticationSecurityWebFilter;

@Component
public class MyAnonymousAuthenticationSecurityWebFilter
        implements AnonymousAuthenticationSecurityWebFilter {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    // Do your logic here
    return chain.filter(exchange);
  }
}
```

## 前置过滤器（BeforeSecurityWebFilter）

主要用于在进行认证之前的一些处理。需要注意的是，当前过滤器中无法直接通过 ReactiveSecurityContextHolder 获取
SecurityContext。示例如下：

```java
public class MyBeforeSecurityWebFilter implements BeforeSecurityWebFilter {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    // Do your logic here
    return chain.filter(exchange);
  }
}
```

## 后置过滤器（AfterSecurityWebFilter）

主要用于进行认证之后的一些处理。在当前过滤器中，可以通过 ReactiveSecurityContextHolder 获取 SecurityContext。示例如下：

```java
public class MyAfterSecurityWebFilter implements AfterSecurityWebFilter {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    return ReactiveSecurityContextHolder.getContext()
            .switchIfEmpty(Mono.defer(() -> {
              // do something...
              return chain.filter(exchange).then(Mono.empty());
            }))
            .flatMap(securityContext -> {
              // do something...
              return chain.filter(exchange);
            });
  }
}
```

---

我们在实现扩展点的时候需要注意：如果当前请求不满足认证条件，请一定要调用 `chain.filter(exchange)`，给其他 filter 留下机会。

后续会根据需求实现其他认证相关的扩展点。