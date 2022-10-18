# 自定义 FormKit 输入组件

## 原由

目前不管是在 Console 中，还是在插件 / 主题设置表单中，都有可能选择系统当中的资源，所以可以通过自定义 FormKit 组件的方式提供常用的选择器。

## 使用方式

目前已提供以下类型：

- `menuCheckbox`：选择一组菜单
- `menuRadio`：选择一个菜单
- `menuItemSelect`：选择菜单项
- `postSelect`：选择文章
- `singlePageSelect`：选择自定义页面
- `categorySelect`：选择分类
- `categoryCheckbox`：选择多个分类
- `tagSelect`：选择标签
- `tagCheckbox`：选择多个标签

在 Vue 单组件中使用：

```vue
<script lang="ts" setup>
const postName = ref("")
</script>

<template>
  <FormKit
    v-model="postName"
    placeholder="请选择文章"
    label="文章"
    type="postSelect"
    validation="required"
  />
</template>
```

在 FormKit Schema 中使用（插件 / 主题设置表单定义）：

```yaml
- $formkit: menuRadio
  name: menus
  label: 底部菜单组
```
