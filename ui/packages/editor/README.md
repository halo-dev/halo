# @halo-dev/richtext-editor

The default editor for Halo, built with [Tiptap](https://tiptap.dev/) + [ProseMirror](https://prosemirror.net/).

> ⚠️ This package is not suitable for general editor use cases, as it is tightly coupled with Halo and uses some of Halo's built-in tools and shared dependencies.
> However, you can use it in Halo plugins just like you would use it in Halo itself.

## Installation

In Halo plugins:

```bash
pnpm install @halo-dev/richtext-editor
```

## Usage

```vue
<script setup lang="ts">
import {
  ExtensionsKit,
  RichTextEditor,
  VueEditor,
} from "@halo-dev/richtext-editor";
import { onMounted, ref, shallowRef } from "vue";

const editor = shallowRef<VueEditor>();
const content = ref("Hello World");

onMounted(() => {
  editor.value = new VueEditor({
    content: content.value,
    extensions: [ExtensionsKit],
    parseOptions: {
      preserveWhitespace: true,
    },
    onUpdate: () => {
      content.value = editor.value?.getHTML() || "";
    },
  });
});
</script>
<template>
  <RichTextEditor v-if="editor" :editor="editor" />
</template>
```

## Requirements

- Vue 3.5.x or higher
- Halo plugin environment

## Links

- [Halo](https://github.com/halo-dev/halo)
- [Documentation](https://docs.halo.run)
- [Report Issues](https://github.com/halo-dev/halo/issues)

## License

GPL-3.0
