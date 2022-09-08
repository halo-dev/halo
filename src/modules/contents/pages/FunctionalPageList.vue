<script lang="ts" setup>
import { ref } from "vue";
import {
  VEmpty,
  VSpace,
  VButton,
  IconAddCircle,
  IconSettings,
  VTag,
} from "@halo-dev/components";
import type { PagesPublicState } from "@halo-dev/admin-shared";
import { useExtensionPointsState } from "@/composables/usePlugins";

const pagesPublicState = ref<PagesPublicState>({
  functionalPages: [],
});

useExtensionPointsState("PAGES", pagesPublicState);
</script>

<template>
  <VEmpty
    v-if="!pagesPublicState.functionalPages.length"
    message="当前没有功能页面，功能页面通常由各个插件提供，你可以尝试安装新插件以获得支持"
    title="当前没有功能页面"
  >
    <template #actions>
      <VSpace>
        <VButton :route="{ name: 'Plugins' }" type="primary">
          <template #icon>
            <IconAddCircle class="h-full w-full" />
          </template>
          安装插件
        </VButton>
      </VSpace>
    </template>
  </VEmpty>
  <ul
    v-else
    class="box-border h-full w-full divide-y divide-gray-100"
    role="list"
  >
    <li
      v-for="(page, index) in pagesPublicState.functionalPages"
      :key="index"
      v-permission="page.permissions"
      @click="$router.push({ path: page.path })"
    >
      <div
        class="relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
      >
        <div class="relative flex flex-row items-center">
          <div class="flex-1">
            <div class="flex flex-row">
              <span
                class="mr-0 truncate text-sm font-medium text-gray-900 sm:mr-2"
              >
                {{ page.name }}
              </span>
              <VTag>{{ page.url }}</VTag>
            </div>
          </div>
          <div class="flex">
            <div
              class="inline-flex flex-col items-end gap-4 sm:flex-row sm:items-center sm:gap-6"
            >
              <span class="cursor-pointer">
                <IconSettings />
              </span>
            </div>
          </div>
        </div>
      </div>
    </li>
  </ul>
</template>
