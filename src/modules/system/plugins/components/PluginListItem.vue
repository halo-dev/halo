<script lang="ts" setup>
import {
  VButton,
  VSpace,
  VSwitch,
  VTag,
  VStatusDot,
  VEntity,
  VEntityField,
} from "@halo-dev/components";
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
  <VEntity>
    <template #start>
      <VEntityField>
        <template #description>
          <div class="h-12 w-12 rounded border bg-white p-1 hover:shadow-sm">
            <img
              :alt="plugin?.metadata.name"
              :src="plugin?.spec.logo"
              class="h-full w-full"
            />
          </div>
        </template>
      </VEntityField>
      <VEntityField
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
      </VEntityField>
    </template>
    <template #end>
      <VEntityField v-if="plugin?.status?.phase === 'FAILED'">
        <template #description>
          <VStatusDot
            v-tooltip="`${plugin?.status?.reason}:${plugin?.status?.message}`"
            state="error"
            animate
          />
        </template>
      </VEntityField>
      <VEntityField>
        <template #description>
          <a
            :href="plugin?.spec.homepage"
            class="hidden text-sm text-gray-500 hover:text-gray-900 sm:block"
            target="_blank"
          >
            @{{ plugin?.spec.author }}
          </a>
        </template>
      </VEntityField>
      <VEntityField :description="plugin?.spec.version" />
      <VEntityField v-if="plugin?.metadata.creationTimestamp">
        <template #description>
          <span class="truncate text-xs tabular-nums text-gray-500">
            {{ formatDatetime(plugin?.metadata.creationTimestamp) }}
          </span>
        </template>
      </VEntityField>
      <VEntityField>
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
      </VEntityField>
    </template>
    <template #dropdownItems>
      <VButton v-close-popper block type="danger" @click="uninstall">
        卸载
      </VButton>
    </template>
  </VEntity>
</template>
