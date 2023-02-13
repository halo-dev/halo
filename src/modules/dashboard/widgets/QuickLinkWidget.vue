<script lang="ts" setup>
import {
  IconArrowRight,
  IconBookRead,
  IconFolder,
  IconPages,
  IconPlug,
  IconUserSettings,
  IconPalette,
  IconWindowLine,
  VCard,
  IconUserLine,
} from "@halo-dev/components";
import { markRaw, ref, type Component } from "vue";
import { useRouter } from "vue-router";
import ThemePreviewModal from "@/modules/interface/themes/components/preview/ThemePreviewModal.vue";

interface Action {
  icon: Component;
  title: string;
  action: () => void;
  permissions?: string[];
}

const router = useRouter();

const themePreviewVisible = ref(false);

const actions: Action[] = [
  {
    icon: markRaw(IconUserLine),
    title: "个人资料",
    action: () => {
      router.push({
        name: "UserDetail",
        params: { name: "-" },
      });
    },
  },
  {
    icon: markRaw(IconWindowLine),
    title: "查看站点",
    action: () => {
      themePreviewVisible.value = true;
    },
    permissions: ["system:themes:view"],
  },
  {
    icon: markRaw(IconBookRead),
    title: "创建文章",
    action: () => {
      router.push({
        name: "PostEditor",
      });
    },
    permissions: ["system:posts:manage"],
  },
  {
    icon: markRaw(IconPages),
    title: "创建页面",
    action: () => {
      router.push({
        name: "SinglePageEditor",
      });
    },
    permissions: ["system:singlepages:manage"],
  },
  {
    icon: markRaw(IconFolder),
    title: "附件上传",
    action: () => {
      router.push({
        name: "Attachments",
        query: {
          action: "upload",
        },
      });
    },
    permissions: ["system:attachments:manage"],
  },
  {
    icon: markRaw(IconPalette),
    title: "主题管理",
    action: () => {
      router.push({
        name: "ThemeDetail",
      });
    },
    permissions: ["system:themes:view"],
  },
  {
    icon: markRaw(IconPlug),
    title: "插件管理",
    action: () => {
      router.push({
        name: "Plugins",
      });
    },
    permissions: ["system:plugins:view"],
  },
  {
    icon: markRaw(IconUserSettings),
    title: "新建用户",
    action: () => {
      router.push({
        name: "Users",
        query: {
          action: "create",
        },
      });
    },
    permissions: ["system:users:manage"],
  },
];
</script>
<template>
  <VCard
    :body-class="['h-full', 'overflow-y-auto', '@container']"
    class="h-full"
    title="快捷访问"
  >
    <div
      class="grid grid-cols-1 gap-2 overflow-hidden @sm:grid-cols-2 @md:grid-cols-3"
    >
      <div
        v-for="(action, index) in actions"
        :key="index"
        v-permission="action.permissions"
        class="group relative cursor-pointer rounded-lg bg-gray-50 p-4 transition-all hover:bg-gray-100"
        @click="action.action"
      >
        <div>
          <span
            class="inline-flex rounded-lg bg-teal-50 p-3 text-teal-700 ring-4 ring-white"
          >
            <component :is="action.icon"></component>
          </span>
        </div>
        <div class="mt-8">
          <h3 class="text-sm font-semibold">
            {{ action.title }}
          </h3>
        </div>
        <span
          aria-hidden="true"
          class="pointer-events-none absolute top-6 right-6 text-gray-300 transition-all group-hover:translate-x-1 group-hover:text-gray-400"
        >
          <IconArrowRight />
        </span>
      </div>
    </div>
  </VCard>
  <ThemePreviewModal v-model:visible="themePreviewVisible" title="查看站点" />
</template>
