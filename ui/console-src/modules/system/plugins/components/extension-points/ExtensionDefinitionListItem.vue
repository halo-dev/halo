<script lang="ts" setup>
import {
  coreApiClient,
  type ExtensionDefinition,
  type Plugin,
} from "@halo-dev/api-client";
import {
  IconSettings,
  VAvatar,
  VEntity,
  VEntityField,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { computed, ref } from "vue";
import PluginDetailModal from "../PluginDetailModal.vue";

const props = withDefaults(
  defineProps<{ extensionDefinition: ExtensionDefinition }>(),
  {}
);

const { data: plugins } = useQuery<Plugin[]>({
  queryKey: ["extension-definition-related-plugins"],
  queryFn: async () => {
    const { data } = await coreApiClient.plugin.plugin.listPlugin({
      page: 0,
      size: 0,
    });
    return data.items;
  },
});

const matchedPlugin = computed(() => {
  return plugins.value?.find(
    (plugin) =>
      plugin.metadata.name ===
      props.extensionDefinition.metadata.labels?.["plugin.halo.run/plugin-name"]
  );
});

const pluginDetailModalVisible = ref(false);
</script>

<template>
  <PluginDetailModal
    v-if="pluginDetailModalVisible && matchedPlugin"
    :name="matchedPlugin.metadata.name"
    @close="pluginDetailModalVisible = false"
  />
  <VEntity>
    <template v-if="$slots['selection-indicator']" #checkbox>
      <slot name="selection-indicator" />
    </template>
    <template #start>
      <VEntityField>
        <template #description>
          <VAvatar
            :alt="extensionDefinition.spec.displayName"
            :src="extensionDefinition.spec.icon || matchedPlugin?.status?.logo"
            size="md"
          ></VAvatar>
        </template>
      </VEntityField>
      <VEntityField
        :title="extensionDefinition.spec.displayName"
        :description="extensionDefinition.spec.description"
      ></VEntityField>
    </template>
    <template v-if="matchedPlugin" #end>
      <VEntityField>
        <template #description>
          <div
            class="cursor-pointer rounded p-1 text-gray-600 transition-all hover:text-blue-600 group-hover:bg-gray-200/60"
            @click.prevent="pluginDetailModalVisible = true"
          >
            <IconSettings />
          </div>
        </template>
      </VEntityField>
    </template>
  </VEntity>
</template>
