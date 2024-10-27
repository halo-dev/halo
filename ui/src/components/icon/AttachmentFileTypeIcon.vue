<script lang="ts" setup>
import { extname } from "path-browserify";
import { computed, markRaw } from "vue";
import VscodeIconsDefaultFile from "~icons/vscode-icons/default-file";
import VscodeIconsFileTypeAi from "~icons/vscode-icons/file-type-ai";
import VscodeIconsFileTypeAstro from "~icons/vscode-icons/file-type-astro";
import VscodeIconsFileTypeBat from "~icons/vscode-icons/file-type-bat";
import VscodeIconsFileTypeC from "~icons/vscode-icons/file-type-c";
import VscodeIconsFileTypeClass from "~icons/vscode-icons/file-type-class";
import VscodeIconsFileTypeCpp from "~icons/vscode-icons/file-type-cpp";
import VscodeIconsFileTypeCss from "~icons/vscode-icons/file-type-css";
import VscodeIconsFileTypeDb from "~icons/vscode-icons/file-type-db";
import VscodeIconsFileTypeExcel from "~icons/vscode-icons/file-type-excel";
import VscodeIconsFileTypeGo from "~icons/vscode-icons/file-type-go";
import VscodeIconsFileTypeGradle from "~icons/vscode-icons/file-type-gradle";
import VscodeIconsFileTypeHtml from "~icons/vscode-icons/file-type-html";
import VscodeIconsFileTypeImage from "~icons/vscode-icons/file-type-image";
import VscodeIconsFileTypeJar from "~icons/vscode-icons/file-type-jar";
import VscodeIconsFileTypeJava from "~icons/vscode-icons/file-type-java";
import VscodeIconsFileTypeJsOfficial from "~icons/vscode-icons/file-type-js-official";
import VscodeIconsFileTypeJson from "~icons/vscode-icons/file-type-json";
import VscodeIconsFileTypeMarkdown from "~icons/vscode-icons/file-type-markdown";
import VscodeIconsFileTypePhotoshop from "~icons/vscode-icons/file-type-photoshop";
import VscodeIconsFileTypePhp3 from "~icons/vscode-icons/file-type-php3";
import VscodeIconsFileTypePowerpoint from "~icons/vscode-icons/file-type-powerpoint";
import VscodeIconsFileTypePython from "~icons/vscode-icons/file-type-python";
import VscodeIconsFileTypeShell from "~icons/vscode-icons/file-type-shell";
import VscodeIconsFileTypeText from "~icons/vscode-icons/file-type-text";
import VscodeIconsFileTypeTypescriptOfficial from "~icons/vscode-icons/file-type-typescript-official";
import VscodeIconsFileTypeVue from "~icons/vscode-icons/file-type-vue";
import VscodeIconsFileTypeWord from "~icons/vscode-icons/file-type-word";
import VscodeIconsFileTypeXml from "~icons/vscode-icons/file-type-xml";
import VscodeIconsFileTypeYaml from "~icons/vscode-icons/file-type-yaml";

const FileTypeIconsMap = {
  // image
  ".jpg": markRaw(VscodeIconsFileTypeImage),
  ".jpeg": markRaw(VscodeIconsFileTypeImage),
  ".png": markRaw(VscodeIconsFileTypeImage),
  ".gif": markRaw(VscodeIconsFileTypeImage),
  ".webp": markRaw(VscodeIconsFileTypeImage),
  ".svg": markRaw(VscodeIconsFileTypeImage),

  // document
  ".docx": markRaw(VscodeIconsFileTypeWord),
  ".pptx": markRaw(VscodeIconsFileTypePowerpoint),
  ".xlsx": markRaw(VscodeIconsFileTypeExcel),
  ".psd": markRaw(VscodeIconsFileTypePhotoshop),
  ".ai": markRaw(VscodeIconsFileTypeAi),
  ".txt": markRaw(VscodeIconsFileTypeText),

  // programming languages or frameworks
  ".json": markRaw(VscodeIconsFileTypeJson),
  ".html": markRaw(VscodeIconsFileTypeHtml),
  ".yaml": markRaw(VscodeIconsFileTypeYaml),
  ".xml": markRaw(VscodeIconsFileTypeXml),
  ".java": markRaw(VscodeIconsFileTypeJava),
  ".jar": markRaw(VscodeIconsFileTypeJar),
  ".class": markRaw(VscodeIconsFileTypeClass),
  ".js": markRaw(VscodeIconsFileTypeJsOfficial),
  ".ts": markRaw(VscodeIconsFileTypeTypescriptOfficial),
  ".vue": markRaw(VscodeIconsFileTypeVue),
  ".go": markRaw(VscodeIconsFileTypeGo),
  ".c": markRaw(VscodeIconsFileTypeC),
  ".cpp": markRaw(VscodeIconsFileTypeCpp),
  ".astro": markRaw(VscodeIconsFileTypeAstro),
  ".bat": markRaw(VscodeIconsFileTypeBat),
  ".css": markRaw(VscodeIconsFileTypeCss),
  ".db": markRaw(VscodeIconsFileTypeDb),
  ".gradle": markRaw(VscodeIconsFileTypeGradle),
  ".md": markRaw(VscodeIconsFileTypeMarkdown),
  ".py": markRaw(VscodeIconsFileTypePython),
  ".sh": markRaw(VscodeIconsFileTypeShell),
  ".php": markRaw(VscodeIconsFileTypePhp3),
};

const props = withDefaults(
  defineProps<{
    fileName: string | undefined;
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
  return markRaw(VscodeIconsDefaultFile);
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
