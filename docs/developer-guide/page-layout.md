# 页面布局契约

前台页面可以通过主题提供的 `templates/layout.html` 复用当前主题的页面外壳。这个契约是增量能力：主题不提供或提供异常时，Halo 会使用系统内置的 fallback 布局，主题仍然可以正常安装、升级、启用和渲染。

## 主题开发者

主题可以在根模板目录提供 `templates/layout.html`，并声明 `html(head, content)` 片段：

```html
<!doctype html>
<html xmlns:th="https://www.thymeleaf.org" th:lang="${#locale.toLanguageTag}" th:fragment="html (head, content)">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <th:block th:replace="${head}" />
  </head>
  <body>
    <th:block th:replace="~{modules/header}" />
    <th:block th:replace="${content}" />
    <th:block th:replace="~{modules/footer}" />
    <halo:footer />
  </body>
</html>
```

`head` 应插入到 `<head>` 中，`content` 应插入到 `<body>` 中。主题可以自行决定外层容器、页头、页脚、暗黑模式、响应式布局等实现细节。

## 插件开发者

插件模板可以通过 `layout :: html(...)` 调用当前主题或系统 fallback 布局：

```html
<!doctype html>
<html
  xmlns:th="https://www.thymeleaf.org"
  th:replace="~{layout :: html(head = ~{::head}, content = ~{::content})}"
>
  <th:block th:fragment="head">
    <title>插件页面标题</title>
  </th:block>
  <th:block th:fragment="content">
    <main>插件页面正文</main>
  </th:block>
</html>
```

当插件模板由插件自身提供时，`layout` 是 Halo 保留的集成模板名。即使插件包内也存在 `templates/layout.html`，也不会被用于满足这个主题布局契约。插件内部私有布局请使用其他模板名。

## 兼容状态

主题安装、更新或重载后，Halo 会检查 `templates/layout.html` 并在 `Theme.status.pageLayout` 中记录状态：

- `SUPPORTED`：主题提供了符合 `html(head, content)` 契约的布局。
- `MISSING`：主题未提供 `templates/layout.html`，使用布局契约的页面将使用 Halo 的 fallback 布局。
- `INVALID`：主题提供了 `templates/layout.html`，但签名不符合 v1 契约。状态中会包含简短诊断原因。

缺失或异常不会让主题进入失败状态。Console 会展示这些状态，用于提示用户和主题开发者逐步完成适配。

## 版本演进

当前 v1 契约只包含 `head` 和 `content` 两个片段参数。后续如果需要新增插槽，应优先考虑新片段名或新契约版本，避免让已适配的主题因为新增必填参数而渲染失败。
