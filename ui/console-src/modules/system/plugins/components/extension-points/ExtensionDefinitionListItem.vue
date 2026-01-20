<script lang="ts" setup>
import {
  coreApiClient,
  paginate,
  type ExtensionDefinition,
  type Plugin,
  type PluginV1alpha1ApiListPluginRequest,
} from "@halo-dev/api-client";
import {
  IconSettings,
  VAvatar,
  VEntity,
  VEntityField,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { computed, ref } from "vue";

const props = withDefaults(
  defineProps<{ extensionDefinition: ExtensionDefinition }>(),
  {}
);

const { data: plugins } = useQuery<Plugin[]>({
  queryKey: ["extension-definition-related-plugins"],
  queryFn: async () => {
    return await paginate<PluginV1alpha1ApiListPluginRequest, Plugin>(
      (params) => coreApiClient.plugin.plugin.listPlugin(params),
      {
        size: 1000,
      }
    );
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
    <template #end>
      <VEntityField v-if="matchedPlugin">
        <template #description>
          <div
            class="cursor-pointer rounded p-1 text-gray-600 transition-all hover:text-blue-600 group-hover:bg-gray-200/60"
            @click.prevent="pluginDetailModalVisible = true"
          >
            <IconSettings />
          </div>
        </template>
      </VEntityField>
      <slot name="end" />
    </template>
  </VEntity>
</template>
