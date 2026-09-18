# 评论编辑器扩展点

用于替换 Halo 在 Console 的默认评论输入框。

> 注意：
> 此扩展点并非通用扩展点，由于 Halo 早期设定，Halo 在前台的评论组件 UI 部分由 [评论组件插件](http://github.com/halo-dev/plugin-comment-widget) 提供，而在此插件的后续版本中提供了富文本编辑器的功能，所以为了保持 Console 的评论输入框与前台一致，所以专为此插件提供了替换输入框的扩展点。

## 定义方式

```ts
import { definePlugin } from "@halo-dev/ui-shared";
import { markRaw } from "vue";
import CommentEditor from "./components/CommentEditor.vue";

export default definePlugin({
  extensionPoints: {
    "comment:editor:replace": () => {
      return {
        component: markRaw(CommentEditor),
        supportsEditing: true,
      };
    },
  },
});
```

其中，组件需要包含的 props 如下：

1. `autoFocus`：是否自动聚焦，需要在组件中判断是否为 `true`，然后聚焦输入框。
2. `initialContent`：可选的初始正文，在组件创建时回填。编辑已有评论时会传入保存的 `spec.raw`，新建回复时不传。回填需要保留原有 HTML 格式，不能在异步加载或父组件刷新时清空用户的修改。

组件实现回填后，提供者应声明 `supportsEditing: true`。未声明时，新建回复仍使用该组件，编辑已有评论则使用默认正文源码输入框，保留原文及 HTML 标签。

需要定义的 emit 如下：

1. `(event: "update", value: { content: string; characterCount: number })`：向调用方传递内容和字符数更新的事件。
