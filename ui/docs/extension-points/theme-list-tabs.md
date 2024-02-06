# 主题管理界面选项卡扩展点

## 原由

目前在 Halo 的主题管理中原生支持本地上传和远程下载的方式安装主题，此扩展点用于扩展主题管理界面的选项卡，以支持更多的安装方式。

## 定义方式

> 此示例为添加一个安装选项卡用于从 GitHub 上下载主题。

```ts
import { definePlugin } from "@halo-dev/console-shared";
import { markRaw } from "vue";
import GitHubDownloadTab from "./components/GitHubDownloadTab.vue";

export default definePlugin({
  extensionPoints: {
    "theme:list:tabs:create": () => {
      return [
        {
          id: "github",
          label: "GitHub",
          component: markRaw(GitHubDownload),
          props: {
            foo: "bar",
          },
          priority: 11,
        },
      ];
    },
  },
});
```

扩展点类型：

```ts
"theme:list:tabs:create"?: () =>
  | ThemeListTab[]
  | Promise<ThemeListTab[]>;
```

`ThemeListTab`:

```ts
export interface ThemeListTab {
  id: string;                         // 选项卡的唯一标识
  label: string;                      // 选项卡的名称
  component: Raw<Component>;          // 选项卡面板的组件
  props?: Record<string, unknown>;    // 选项卡组件的 props
  permissions?: string[];             // 权限
  priority: number;                   // 优先级
}
```
