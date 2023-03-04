# 路由和 Console 端菜单的生成

## 简述

目前的路由以及菜单都是动态生成的，由 `基础路由`、`核心模块路由`、`插件模块路由` 三部分组成。

定义文件位置：

- 基础路由：`src/router/routes.config.ts`,
- 核心模块路由：`src/modules/**/module.ts`,

## 定义方式

统一由 `@halo-dev/console-shared` 包中的 `definePlugin` 方法配置。如：

```ts
import { definePlugin } from "@halo-dev/console-shared";
import BasicLayout from "@/layouts/BasicLayout.vue";
import AttachmentList from "./AttachmentList.vue";
import AttachmentSelectorModal from "./components/AttachmentSelectorModal.vue";
import { IconFolder } from "@halo-dev/components";
import { markRaw } from "vue";

export default definePlugin({
  name: "attachmentModule",
  components: [AttachmentSelectorModal],
  routes: [
    {
      path: "/attachments",
      component: BasicLayout,
      children: [
        {
          path: "",
          name: "Attachments",
          component: AttachmentList,
          meta: {
            title: "附件",
            permissions: ["system:attachments:view"],
            menu: {
              name: "附件",
              group: "content",
              icon: markRaw(IconFolder),
              priority: 3,
              mobile: true,
            },
          },
        },
      ],
    },
  ],
});
```

其中，如果要将路由添加到侧边的菜单，那么需要在 `meta` 中定义好 `menu` 对象，menu 对象类型详解如下：

```ts
interface RouteMeta {
  title?: string;
  searchable?: boolean;
  permissions?: string[];
  core?: boolean;
  menu?: {
    name: string;               // 菜单名称
    group?: CoreMenuGroupId;    // 菜单分组 ID，详见下方 CoreMenuGroupId 定义
    icon?: Component;           // 菜单图标，类型为 Vue 组件，可以使用 `@halo-dev/components` 包中的图标组件，或者自行接入 https://github.com/antfu/unplugin-icons
    priority: number;           // 排序字段，相对于 group，插件中提供的菜单将始终放在最后
    mobile?: boolean;           // 是否添加到移动端底部的菜单
  };
}
```

CoreMenuGroupId：

```ts
declare type CoreMenuGroupId = "dashboard" | "content" | "interface" | "system" | "tool";
```

这是核心内置的菜单分组，但如果插件需要自定义分组，可以直接填写分组名，如：

```ts
{
  name: "帖子",
  group: "社区",
  icon: markRaw(IconCummunity),
  priority: 1,
  mobile: false,
}
```

## 插件接入

定义方式与系统核心模块的定义方式一致，在 `definePlugin` 方法配置即可。主要额外注意的是，如果插件的路由需要基础布局（继承 BasicLayout），需要配置 `parentName`，如：

```ts
export default definePlugin({
  routes: [
    {
      parentName: "Root",
      route: {
        path: "/migrate",
        children: [
          {
            path: "",
            name: "Migrate",
            component: MigrateView,
            meta: {
              title: "迁移",
              searchable: true,
              menu: {
                name: "迁移",
                group: "tool",
                icon: markRaw(IconGrid),
                priority: 0,
              },
            },
          },
        ],
      },
    },
  ]
})
```

## 权限

在 `meta` 中配置 `permissions` 即可。类型为 UI 权限标识的数组，如 `["system:attachments:view"]`。如果当前用户没有对应权限，那么将不会注册路由和菜单。
