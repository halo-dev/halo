<script lang="ts" name="QuickLinkWidget" setup>
import {
  IconArrowRight,
  IconBookRead,
  IconFolder,
  IconPalette,
  IconPlug,
  IconUserSettings,
  VCard,
} from "@halo-dev/components";
import { markRaw, type Component } from "vue";
import { useRouter } from "vue-router";
import type { RouteLocationRaw } from "vue-router";

interface Action {
  icon: Component;
  title: string;
  route: RouteLocationRaw;
}

const actions: Action[] = [
  {
    icon: markRaw(IconBookRead),
    title: "创建文章",
    route: {
      name: "PostEditor",
    },
  },
  {
    icon: markRaw(IconFolder),
    title: "附件上传",
    route: {
      name: "Attachments",
      query: {
        action: "upload",
      },
    },
  },
  {
    icon: markRaw(IconPalette),
    title: "外观编辑",
    route: {
      name: "ThemeVisual",
    },
  },
  {
    icon: markRaw(IconPlug),
    title: "插件管理",
    route: {
      name: "Plugins",
    },
  },
  {
    icon: markRaw(IconUserSettings),
    title: "新建用户",
    route: {
      name: "Users",
      query: {
        action: "create",
      },
    },
  },
];

const router = useRouter();
</script>
<template>
  <VCard
    :body-class="['h-full', 'overflow-y-auto', '!p-0']"
    class="h-full"
    title="快捷访问"
  >
    <div
      class="divide-y divide-x divide-gray-100 overflow-hidden sm:grid sm:grid-cols-3 sm:gap-px sm:divide-y-0"
    >
      <div
        v-for="(action, index) in actions"
        :key="index"
        class="group relative cursor-pointer bg-white p-6 hover:bg-gray-50"
        @click="router.push(action.route)"
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
</template>
