<script lang="ts" setup>
import { ref } from "vue";
import { VEmpty, VSpace, VButton, IconAddCircle } from "@halo-dev/components";
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
    >
      <div
        class="relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
      >
        <div class="relative flex flex-row items-center">
          <div class="flex-1">
            <div class="flex">
              <RouterLink
                :to="page.path"
                class="truncate text-sm font-medium text-gray-900"
              >
                {{ page.name }}
              </RouterLink>
            </div>
            <div class="mt-1 flex">
              <span class="text-xs text-gray-500">
                {{ page.url }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </li>
  </ul>
</template>
