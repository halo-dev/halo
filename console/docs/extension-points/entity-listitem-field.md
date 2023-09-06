# Entity 数据列表显示字段扩展点

## 原由

目前 Halo 2 的 Console 中，展示数据列表是统一使用 Entity 组件，此扩展点用于支持通过插件扩展部分数据列表的显示字段。

## 定义方式

目前支持扩展的数据列表：

- 插件：`"plugin:list-item:field:create"?: (plugin: Ref<Plugin>) => | EntityFieldItem[] | Promise<EntityFieldItem[]>`
- 文章：`"post:list-item:field:create"?: (post: Ref<ListedPost>) => | EntityFieldItem[] | Promise<EntityFieldItem[]>`

示例：

> 此示例是在插件列表项中添加一个显示插件启动时间的字段。

```ts
import { definePlugin } from "@halo-dev/console-shared";
import { markRaw, type Ref } from "vue";
import type { Plugin } from "@halo-dev/api-client";
import { VEntityField } from "@halo-dev/components"

export default definePlugin({
  extensionPoints: {
    "plugin:list-item:field:create": (plugin: Ref<Plugin>) => {
      return [
        {
          priority: 40,
          position: "end",
          component: markRaw(VEntityField),
          props: {
            title: "启动时间"
            description: plugin.value.status.lastStartTime
          },
        },
      ];
    },
  },
});
```

`EntityFieldItem` 类型：

```ts
export interface EntityFieldItem {
  priority: number;                     // 优先级，越小越靠前
  position: "start" | "end";            // 显示字段的位置
  component: Raw<Component>;            // 字段组件，可以使用 `@halo-dev/components` 中提供的 `VEntityField`，也可以自定义
  props?: Record<string, unknown>;      // 组件的 props
  permissions?: string[];               // 权限设置
  hidden?: boolean;                     // 是否隐藏
}
```
