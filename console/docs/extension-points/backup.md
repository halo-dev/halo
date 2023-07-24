# 备份页面选项卡扩展点

## 原由

在 Halo 2.8 中提供了基础备份和恢复的功能，此扩展点是为了提供给插件开发者针对备份扩展更多功能，比如定时备份设置、备份到第三方云存储等。

## 定义方式

```ts
import { definePlugin } from "@halo-dev/console-shared";
import BackupStorage from "@/views/BackupStorage.vue";
import { markRaw } from "vue";

export default definePlugin({
  components: {},
  routes: [],
  extensionPoints: {
    "backup:tabs:create": () => {
      return [
        {
          id: "storage",
          label: "备份位置",
          component: markRaw(BackupStorage),
        },
      ];
    },
  },
});
```

BackupTab 类型：

```ts
import type { Component, Raw } from "vue";

export interface BackupTab {
  id: string;
  label: string;
  component: Raw<Component>;
  permissions?: string[];
}

```
