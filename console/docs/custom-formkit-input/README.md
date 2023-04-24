# 自定义 FormKit 输入组件

## 原由

目前在 Console 端的所有表单都使用了 FormKit，但 FormKit 内置的 Input 组件并不满足所有的需求，因此需要自定义一些 Input 组件。此外，为了插件和主题能够更加方便的使用系统内的一些数据，所以同样需要自定义一些带数据的选择组件。

## 使用方式

目前已提供以下类型：

- `code`: 代码编辑器
  - 参数
    1. language: 目前支持 `yaml`, `html`, `css`, `javascript`, `json`
    2. height: 编辑器高度，如：`100px`
- `attachment`: 附件选择
  - 参数
    1. accepts：允许上传的文件类型，如：`image/*`
- `repeater`: 定义一个对象集合，可以让使用者可视化的操作集合。
- `menuCheckbox`：选择一组菜单
- `menuRadio`：选择一个菜单
- `menuItemSelect`：选择菜单项
- `postSelect`：选择文章
- `singlePageSelect`：选择自定义页面
- `categorySelect`：选择分类
  - 参数
    1. multiple: 是否多选，默认为 `false`
- `categoryCheckbox`：选择多个分类
- `tagSelect`：选择标签
  - 参数
    1. multiple: 是否多选，默认为 `false`
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

### Repeater

Repeater 是一个集合类型的输入组件，可以让使用者可视化的操作集合。

在 Vue SFC 中以组件形式使用：

```vue
<script lang="ts" setup>
const users = ref([])
</script>

<template>
  <FormKit
    v-model="users"
    type="repeater"
    label="Users"
  >
    <FormKit
      type="text"
      label="Full Name"
      name="full_name"
      validation="required"
    />
    <FormKit
      type="email"
      label="Email"
      name="email"
      validation="required|email"
    />
  </FormKit>
</template>
```

在 FormKit Schema 中使用：

```yaml
- $formkit: repeater
  name: users
  label: Users
  items:
    - $formkit: text
      name: full_name
      label: Full Name
      validation: required
    - $formkit: email
      name: email
      label: Email
      validation: required|email
```

最终得到的数据类似于：

```json
[
  {
    "full_name": "Jack",
    "email": "jack@example.com"
  },
  {
    "full_name": "John",
    "email": "john@example.com"
  }
]
```
