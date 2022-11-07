<script lang="ts" name="QuickLinkWidget" setup>
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
import { inject, markRaw, ref, type Component } from "vue";
import { useRouter } from "vue-router";
import type { User } from "@halo-dev/api-client";
import ThemePreviewModal from "@/modules/interface/themes/components/preview/ThemePreviewModal.vue";

interface Action {
  icon: Component;
  title: string;
  action: () => void;
  permissions?: string[];
}

const currentUser = inject<User>("currentUser");

const router = useRouter();

const themePreviewVisible = ref(false);

const actions: Action[] = [
  {
    icon: markRaw(IconUserLine),
    title: "个人资料",
    action: () => {
      router.push({
        name: "UserDetail",
        params: { name: currentUser?.metadata.name },
      });
    },
  },
  {
    icon: markRaw(IconWindowLine),
    title: "查看站点",
    action: () => {
      themePreviewVisible.value = true;
    },
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
    :body-class="['h-full', 'overflow-y-auto', '!p-0']"
    class="h-full"
    title="快捷访问"
  >
    <div class="overflow-hidden sm:grid sm:grid-cols-3 sm:gap-px">
      <div
        v-for="(action, index) in actions"
        :key="index"
        v-permission="action.permissions"
        class="group relative cursor-pointer bg-white p-6 transition-all hover:bg-gray-50"
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
          <h3 class="text-base font-medium">
            <span aria-hidden="true" class="absolute inset-0"></span>
            {{ action.title }}
          </h3>
        </div>
        <span
          aria-hidden="true"
          class="pointer-events-none absolute top-6 right-6 text-gray-300 group-hover:text-gray-400"
        >
          <IconArrowRight />
        </span>
      </div>
    </div>
  </VCard>
  <ThemePreviewModal v-model:visible="themePreviewVisible" title="查看站点" />
</template>
