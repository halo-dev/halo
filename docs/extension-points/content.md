# 内容扩展点

## 文章内容扩展点

文章内容扩展点用于在主题端文章内容渲染之前对文章内容进行修改，比如添加广告、添加版权声明、插入脚本等。

## 使用方式

在插件中通过实现 `run.halo.app.theme.ReactivePostContentHandler` 接口来实现文章内容扩展。

以下是一个扩展文章内容支持 Katex 的示例：

```javascript
String katexScript="""
    <link rel="stylesheet" href="/plugins/plugin-katex/assets/static/katex.min.css">
    <script defer src="/plugins/plugin-katex/assets/static/katex.min.js"></script>
    <script defer src="/plugins/plugin-katex/assets/static/contrib/auto-render.min.js"></script>
    <script>
        document.addEventListener("DOMContentLoaded", function() {
            renderMathInElement(document.body, {
              // customised options
              // • auto-render specific keys, e.g.:
              delimiters: [
                  {left: '$$', right: '$$', display: true},
                  {left: '$', right: '$', display: false},
                  {left: '\\(', right: '\\)', display: false},
                  {left: '\\[', right: '\\]', display: true}
              ],
              // • rendering keys, e.g.:
              throwOnError : false
            });
        });
    </script>
    """;
```

然后在 `handle` 方法中将 Katex 的脚本字符串插入到内容前面：

```java

@Component
public class KatexPostContentHandler implements ReactivePostContentHandler {

    @Override
    public Mono<PostContentContext> handle(PostContentContext postContent) {
        postContent.setContent(katexScript + "\n" + postContent.getContent());
        return Mono.just(postContent);
    }
}
```

定义了扩展点实现（扩展），还需要在插件的 `resources/extensions` 目录下添加对扩展的声明：

```yaml
# resources/extensions/extension-definitions.yml
apiVersion: plugin.halo.run/v1alpha1
kind: ExtensionDefinition
metadata:
  name: ext-def-katex-post-content
spec:
  className: run.halo.katex.KatexPostContentHandler
  # 文章内容扩展点的名称，固定值
  extensionPointName: reactive-post-content-handler
  displayName: "KatexPostContentHandler"
  description: "Katex support for post content."
```

## 自定义页面内容扩展点

自定义页面（SinglePage）内容扩展点用于在主题端自定义页面内容渲染之前对内容进行修改，比如添加广告、添加版权声明、插入脚本等。

## 使用方式

在插件中通过实现 `run.halo.app.theme.ReactiveSinglePageContentHandler` 接口来实现内容扩展。

以下是一个扩展内容支持 Katex 的示例：

```java

@Component
public class KatexSinglePageContentHandler implements ReactiveSinglePageContentHandler {

    @Override
    public Mono<SinglePageContentContext> handle(SinglePageContentContext pageContent) {

        String katexScript = ""; // 参考文章内容扩展点的示例脚本块
        pageContent.setContent(katexScript + "\n" + pageContent.getContent());
        return Mono.just(pageContent);
    }
}
```

在插件的 `resources/extensions` 目录下添加对自定义页面内容扩展的声明：

```yaml
# resources/extensions/extension-definitions.yml
apiVersion: plugin.halo.run/v1alpha1
kind: ExtensionDefinition
metadata:
  name: ext-def-katex-singlepage-content
spec:
  className: run.halo.katex.KatexSinglePageContentHandler
  # 自定义页面内容扩展点的名称，固定值
  extensionPointName: reactive-post-content-handler
  displayName: "KatexSinglePageContentHandler"
  description: "Katex support for single page content."
```
