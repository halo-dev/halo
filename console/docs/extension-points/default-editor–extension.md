# 默认编辑器扩展点

该扩展点用于扩展默认编辑器的功能，包括 Tiptap Extension，以及工具栏、悬浮工具栏、Slash Command。

## 定义方式

```ts
import ExtensionFoo from "./tiptap/extension-foo.ts"

export default definePlugin({
  extensionPoints: {
    "default:editor:extension:create": () => {
      return [ExtensionFoo];
    },
  },
});
```

其中，`ExtensionFoo` 是一个 Tiptap Extension，可以参考 [Tiptap 文档](https://tiptap.dev/) 和 [https://github.com/halo-sigs/richtext-editor/blob/main/docs/extension.md](https://github.com/halo-sigs/richtext-editor/blob/main/docs/extension.md)。
