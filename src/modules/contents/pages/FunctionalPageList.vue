<script lang="ts" setup>
import { ref } from "vue";
import { VEmpty, VSpace, VButton, IconAddCircle } from "@halo-dev/components";
import type { PagesPublicState } from "@halo-dev/admin-shared";
import { useExtensionPointsState } from "@/composables/usePlugins";
import Entity from "@/components/entity/Entity.vue";
import EntityField from "@/components/entity/EntityField.vue";

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
      <Entity>
        <template #start>
          <EntityField
            :title="page.name"
            :route="page.path"
            :description="page.url"
          ></EntityField>
        </template>
      </Entity>
    </li>
  </ul>
</template>
