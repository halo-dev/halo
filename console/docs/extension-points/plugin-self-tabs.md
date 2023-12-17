# 插件详情选项卡扩展点

## 原由

部分插件可能需要在 Console 端自行实现 UI 以完成一些自定义的需求，但可能并不希望在菜单中添加一个菜单项，所以希望可以在插件详情页面添加一个自定义 UI 的选项卡。

## 定义方式

```ts
import { definePlugin, PluginTab } from "@halo-dev/console-shared";
import MyComponent from "@/views/my-component.vue";
import { markRaw } from "vue";
export default definePlugin({
  components: {},
  routes: [],
  extensionPoints: {
    "plugin:self:tabs:create": () : PluginTab[] => {
      return [
        {
          id: "my-tab-panel",
          label: "Custom Panel",
          component: markRaw(MyComponent),
          permissions: []
        },
      ];
    },
  },
});
```

PluginTab 类型：

```ts
export interface PluginTab {
  id: string;
  label: string;
  component: Raw<Component>;
  permissions?: string[];
}
```
