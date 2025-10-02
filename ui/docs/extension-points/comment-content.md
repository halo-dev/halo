# 评论列表内容显示扩展点

用于替换 Halo 在 Console 的默认评论列表内容显示组件。

> 注意：
> 此扩展点并非通用扩展点，由于 Halo 早期设定，Halo 在前台的评论组件 UI 部分由 [评论组件插件](http://github.com/halo-dev/plugin-comment-widget) 提供，而在此插件的后续版本中提供了富文本渲染的功能，所以为了保持 Console 的评论列表内容显示与前台一致，所以专为此插件提供了替换输入框的扩展点。

## 定义方式

```ts
import { definePlugin } from "@halo-dev/console-shared";
import { markRaw } from "vue";
import CommentContent from "./components/CommentContent.vue";

export default definePlugin({
  extensionPoints: {
    "comment:list-item:content:replace": () => {
      return {
        component: markRaw(CommentContent),
      };
    },
  },
});
```

其中，组件需要包含的 props 如下：

1. `content`：评论内容，`html` 格式。

