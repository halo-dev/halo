# 插件安装界面选项卡扩展点

## 原由

目前 Halo 原生支持本地上传和远程下载的方式安装插件，此扩展点用于扩展插件安装界面的选项卡，以支持更多的安装方式。

## 定义方式

> 此示例为添加一个安装选项卡用于从 GitHub 上下载插件。

```ts
import { definePlugin } from "@halo-dev/console-shared";
import { markRaw } from "vue";
import GitHubDownloadTab from "./components/GitHubDownloadTab.vue";

export default definePlugin({
  extensionPoints: {
    "plugin:installation:tabs:create": () => {
      return [
        {
          id: "github",
          label: "GitHub",
          component: markRaw(GitHubDownload),
          props: {
            foo: "bar",
          },
          priority: 30,
        },
      ];
    },
  },
});
```

扩展点类型：

```ts
"plugin:installation:tabs:create"?: () =>
  | PluginInstallationTab[]
  | Promise<PluginInstallationTab[]>;
```

`PluginInstallationTab`:

```ts
export interface PluginInstallationTab {
  id: string;                         // 选项卡的唯一标识
  label: string;                      // 选项卡的名称
  component: Raw<Component>;          // 选项卡面板的组件
  props?: Record<string, unknown>;    // 选项卡组件的 props
  permissions?: string[];             // 权限
  priority: number;                   // 优先级
}
```
