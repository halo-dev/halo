# 评论编辑器扩展点

用于替换 Halo 在 Console 的默认评论输入框。

> 注意：
> 此扩展点并非通用扩展点，由于 Halo 早期设定，Halo 在前台的评论组件 UI 部分由 [评论组件插件](http://github.com/halo-dev/plugin-comment-widget) 提供，而在此插件的后续版本中提供了富文本编辑器的功能，所以为了保持 Console 的评论输入框与前台一致，所以专为此插件提供了替换输入框的扩展点。

## 定义方式

```ts
import { definePlugin } from "@halo-dev/console-shared";
import { markRaw } from "vue";
import CommentEditor from "./components/CommentEditor.vue";

export default definePlugin({
  extensionPoints: {
    "comment:editor:replace": () => {
      return {
        component: markRaw(CommentEditor),
      };
    },
  },
});
```

其中，组件需要包含的 props 如下：

1. `autoFocus`：是否自动聚焦，需要在组件中判断是否为 `true`，然后聚焦输入框。

需要定义的 emit 如下：

1. `(event: "update", value: { content: string; characterCount: number })`：向调用方传递内容和字符数更新的事件。
