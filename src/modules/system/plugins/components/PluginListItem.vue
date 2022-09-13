<script lang="ts" setup>
import { VButton, VSpace, VSwitch, VTag } from "@halo-dev/components";
import Entity from "@/components/entity/Entity.vue";
import EntityField from "@/components/entity/EntityField.vue";
import { toRefs } from "vue";
import { usePluginLifeCycle } from "../composables/use-plugin";
import type { Plugin } from "@halo-dev/api-client";
import { formatDatetime } from "@/utils/date";

const props = withDefaults(
  defineProps<{
    plugin?: Plugin;
  }>(),
  {
    plugin: undefined,
  }
);

const { plugin } = toRefs(props);

const { isStarted, changeStatus, uninstall } = usePluginLifeCycle(plugin);
</script>
<template>
  <Entity>
    <template #start>
      <EntityField>
        <template #description>
          <div class="h-12 w-12 rounded border bg-white p-1 hover:shadow-sm">
            <img
              :alt="plugin?.metadata.name"
              :src="plugin?.spec.logo"
              class="h-full w-full"
            />
          </div>
        </template>
      </EntityField>
      <EntityField
        :title="plugin?.spec.displayName"
        :description="plugin?.spec.description"
        :route="{
          name: 'PluginDetail',
          params: { name: plugin?.metadata.name },
        }"
      >
        <template #extra>
          <VSpace>
            <VTag>
              {{ isStarted ? "已启用" : "未启用" }}
            </VTag>
          </VSpace>
        </template>
      </EntityField>
    </template>
    <template #end>
      <EntityField v-if="plugin?.status?.phase === 'FAILED'">
        <template #description>
          <div
            v-tooltip="`${plugin?.status?.reason}:${plugin?.status?.message}`"
            class="inline-flex h-1.5 w-1.5 rounded-full bg-red-600"
          >
            <span
              class="inline-block h-1.5 w-1.5 animate-ping rounded-full bg-red-600"
            ></span>
          </div>
        </template>
      </EntityField>
      <EntityField>
        <template #description>
          <a
            :href="plugin?.spec.homepage"
            class="hidden text-sm text-gray-500 hover:text-gray-900 sm:block"
            target="_blank"
          >
            @{{ plugin?.spec.author }}
          </a>
        </template>
      </EntityField>
      <EntityField :description="plugin?.spec.version" />
      <EntityField
        v-if="plugin?.metadata.creationTimestamp"
        :description="formatDatetime(plugin?.metadata.creationTimestamp)"
      />
      <EntityField>
        <template #description>
          <div
            v-permission="['system:plugins:manage']"
            class="flex items-center"
          >
            <VSwitch
              :model-value="plugin?.spec.enabled"
              @click="changeStatus"
            />
          </div>
        </template>
      </EntityField>
    </template>
    <template #menuItems>
      <VButton v-close-popper block type="danger" @click="uninstall">
        卸载
      </VButton>
    </template>
  </Entity>
</template>
