<script lang="ts" setup>
import { extname } from "path-browserify";
import { computed, defineAsyncComponent } from "vue";

const FileTypeIconsMap = {
  // image
  ".jpg": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-image")
  ),
  ".jpeg": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-image")
  ),
  ".png": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-image")
  ),
  ".gif": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-image")
  ),
  ".webp": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-image")
  ),
  ".svg": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-image")
  ),

  // document
  ".docx": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-word")
  ),
  ".pptx": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-powerpoint")
  ),
  ".xlsx": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-excel")
  ),
  ".psd": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-photoshop")
  ),
  ".ai": defineAsyncComponent(() => import("~icons/vscode-icons/file-type-ai")),
  ".txt": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-text")
  ),

  // programming languages or frameworks
  ".json": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-json")
  ),
  ".html": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-html")
  ),
  ".yaml": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-yaml")
  ),
  ".xml": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-xml")
  ),
  ".java": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-java")
  ),
  ".jar": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-jar")
  ),
  ".class": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-class")
  ),
  ".js": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-js-official")
  ),
  ".ts": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-typescript-official")
  ),
  ".vue": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-vue")
  ),
  ".go": defineAsyncComponent(() => import("~icons/vscode-icons/file-type-go")),
  ".c": defineAsyncComponent(() => import("~icons/vscode-icons/file-type-c")),
  ".cpp": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-cpp")
  ),
  ".astro": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-astro")
  ),
  ".bat": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-bat")
  ),
  ".css": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-css")
  ),
  ".db": defineAsyncComponent(() => import("~icons/vscode-icons/file-type-db")),
  ".gradle": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-gradle")
  ),
  ".md": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-markdown")
  ),
  ".py": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-python")
  ),
  ".sh": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-shell")
  ),
  ".php": defineAsyncComponent(
    () => import("~icons/vscode-icons/file-type-php3")
  ),
};

const props = withDefaults(
  defineProps<{
    fileName?: string;
    displayExt?: boolean;
    width?: number;
    height?: number;
  }>(),
  {
    fileName: undefined,
    displayExt: true,
    width: 10,
    height: 10,
  }
);

const getExtname = computed(() => {
  const ext = extname(props.fileName);
  if (ext) return ext.toLowerCase();
  return undefined;
});

const getIcon = computed(() => {
  const icon = FileTypeIconsMap[getExtname.value];
  if (icon) return icon;
  return defineAsyncComponent(() => import("~icons/vscode-icons/default-file"));
});

const iconClass = computed(() => {
  return [`w-${props.width}`, `h-${props.height}`];
});
</script>
<template>
  <div class="flex h-full w-full flex-col items-center justify-center gap-1">
    <component :is="getIcon" :class="iconClass" />
    <span
      v-if="getExtname && displayExt"
      class="select-none font-sans text-xs text-gray-500"
    >
      {{ getExtname }}
    </span>
  </div>
</template>
