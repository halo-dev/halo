<script lang="ts" setup>
import { onMounted, ref } from "vue";
import {
  VEmpty,
  VSpace,
  VButton,
  IconAddCircle,
  VEntity,
  VEntityField,
} from "@halo-dev/components";
import type { FunctionalPage, PluginModule } from "@halo-dev/console-shared";
import { usePluginModuleStore } from "@/stores/plugin";

const functionalPages = ref<FunctionalPage[]>([] as FunctionalPage[]);

// resolve plugin extension points
const { pluginModules } = usePluginModuleStore();

onMounted(() => {
  pluginModules.forEach((pluginModule: PluginModule) => {
    const { extensionPoints } = pluginModule;
    if (!extensionPoints?.["page:functional:create"]) {
      return;
    }

    const pages = extensionPoints[
      "page:functional:create"
    ]() as FunctionalPage[];

    if (pages) {
      pages.forEach((page) => {
        functionalPages.value.push(page);
      });
    }
  });
});
</script>

<template>
  <VEmpty
    v-if="!functionalPages.length"
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
      v-for="(page, index) in functionalPages"
      :key="index"
      v-permission="page.permissions"
    >
      <VEntity>
        <template #start>
          <VEntityField
            :title="page.name"
            :route="page.path"
            :description="page.url"
          ></VEntityField>
        </template>
      </VEntity>
    </li>
  </ul>
</template>
