# 插件外部配置

插件外部配置功能允许用户在特定目录添加插件相关的配置，插件启动的时候能够自动读取到该配置。

## 配置优先级

> 优先级从上到下由高到低。

1. `${halo.work-dir}/plugins/configs/${plugin-id}.{yaml|yml}`
2. `classpath:/config.{yaml|yml}`

插件开发者可在 `Class Path` 下 添加 `config.{yaml|yml}` 作为默认配置。当 `.yaml` 和 `.yml` 同时出现时，以 `.yml` 的配置将会被忽略。

## 插件中定义配置并使用

- `src/main/java/my/plugin/MyPluginProperties.java`

    ```java
    @Data
    @ConfigurationProperties
    public class MyPluginProperties {
    
        private String encryptKey;
    
        private String certPath;
    }
    ```

- `src/main/java/my/plugin/MyPluginConfiguration.java`

    ```java
    @EnableConfigurationProperties(MyPluginProperties.class)
    @Configuration
    public class MyPluginConfiguration {
        
    }
    ```

- `src/main/java/my/plugin/MyPlugin.java`

    ```java
    @Component
    @Slf4j
    public class MyPlugin extends BasePlugin {
    
        private final MyPluginProperties storeProperties;
    
        public MyPlugin(PluginWrapper wrapper, MyPluginProperties storeProperties) {
            super(wrapper);
            this.storeProperties = storeProperties;
        }
    
        @Override
        public void start() {
            log.info("My plugin properties: {}", storeProperties);
        }
    }
    ```

- `src/main/resources/config.yaml`

    ```yaml
    encryptKey: encrytkey==
    certPath: /path/to/cert
    ```

## 插件使用者配置

- `${halo.work-dir}/plugins/configs/${plugin-id}.{yaml|yml}`

    ```yaml
    encryptKey: override encrytkey==
    certPath: /another/path/to/cert
    ```

## 可能存在的问题

- 增加未来实现"集群"架构的难度。
