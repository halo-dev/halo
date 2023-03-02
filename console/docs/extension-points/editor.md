# 编辑器集成扩展点

## 定义方式

```ts
import MarkdownEditor from "./components/MarkdownEditor.vue"

export default definePlugin({
  extensionPoints: {
    "editor:create": () => {
      return [
        {
          name: "markdown-editor",
          displayName: "Markdown",
          component: markRaw(MarkdownEditor),
          rawType: "markdown",
        },
      ];
    },
  },
});
```

- name: 编辑器名称，用于标识编辑器
- displayName: 编辑器显示名称
- component: 编辑器组件
- rawType: 编辑器支持的原始类型，可以完全由插件定义。但必须保证最终能够将渲染后的 html 设置到 content 中。

## 组件

组件必须设置两个 `v-model` 绑定。即 `v-model:raw` 和 `v-model:content`，以下是示例：

```vue
<template>
  <div>
    <textarea :value="raw" @input="onRawUpdate" />
    <div v-html="content" />
  </div>
</template>

<script lang="ts" setup>
import { watch } from "vue";
import marked from "marked";

const props = withDefaults(
  defineProps<{
    raw?: string;
    content: string;
  }>(),
  {
    raw: "",
    content: "",
  }
);

const emit = defineEmits<{
  (event: "update:raw", value: string): void;
  (event: "update:content", value: string): void;
}>();

function onRawUpdate(e: Event) {
  const raw = (e.target as HTMLTextAreaElement).value;
  emit("update:raw", raw);
}

watch(
  () => props.raw,
  () => {
    emit("update:content", marked(props.raw));
  }
);
</script>
```
