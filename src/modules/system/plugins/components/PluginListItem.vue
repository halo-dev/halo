<script lang="ts" setup>
import {
  IconSettings,
  VButton,
  VSpace,
  VSwitch,
  VTag,
} from "@halo-dev/components";
import type { PropType } from "vue";
import { toRefs } from "vue";
import { usePluginLifeCycle } from "../composables/use-plugin";
import type { Plugin } from "@halo-dev/api-client";

const props = defineProps({
  plugin: {
    type: Object as PropType<Plugin | null>,
    default: null,
  },
});

const { plugin } = toRefs(props);

const { isStarted, changeStatus, uninstall } = usePluginLifeCycle(plugin);
</script>
<template>
  <div
    class="relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
  >
    <div class="relative flex flex-row items-center">
      <div v-if="plugin?.spec.logo" class="mr-4">
        <RouterLink
          :to="{
            name: 'PluginDetail',
            params: { name: plugin?.metadata.name },
          }"
        >
          <div class="h-12 w-12 rounded border bg-white p-1 hover:shadow-sm">
            <img
              :alt="plugin?.metadata.name"
              :src="plugin?.spec.logo"
              class="h-full w-full"
            />
          </div>
        </RouterLink>
      </div>
      <div class="flex-1">
        <div class="flex flex-row items-center">
          <RouterLink
            :to="{
              name: 'PluginDetail',
              params: { name: plugin?.metadata.name },
            }"
          >
            <span class="mr-2 truncate text-sm font-medium text-gray-900">
              {{ plugin?.spec.displayName }}
            </span>
          </RouterLink>
          <VSpace>
            <VTag>
              {{ isStarted ? "已启用" : "未启用" }}
            </VTag>
          </VSpace>
        </div>
        <div class="mt-2 flex">
          <VSpace align="start" direction="column" spacing="xs">
            <span class="text-xs text-gray-500">
              {{ plugin?.spec.description }}
            </span>
            <span class="text-xs text-gray-500 sm:hidden">
              @{{ plugin?.spec.author }} {{ plugin?.spec.version }}
            </span>
          </VSpace>
        </div>
      </div>
      <div class="flex">
        <div
          class="inline-flex flex-col flex-col-reverse items-end gap-4 sm:flex-row sm:items-center sm:gap-6"
        >
          <FloatingTooltip
            v-if="plugin?.status?.phase === 'FAILED'"
            class="hidden items-center sm:flex"
          >
            <div class="inline-flex h-1.5 w-1.5 rounded-full bg-red-600">
              <span
                class="inline-block h-1.5 w-1.5 animate-ping rounded-full bg-red-600"
              ></span>
            </div>
            <template #popper>
              {{ plugin?.status?.reason }}:
              {{ plugin?.status?.message }}
            </template>
          </FloatingTooltip>
          <a
            :href="plugin?.spec.homepage"
            class="hidden text-sm text-gray-500 hover:text-gray-900 sm:block"
            target="_blank"
          >
            @{{ plugin?.spec.author }}
          </a>
          <span class="hidden text-sm text-gray-500 sm:block">
            {{ plugin?.spec.version }}
          </span>
          <time
            class="hidden text-sm text-gray-500 sm:block"
            datetime="2020-01-07"
          >
            {{ plugin?.metadata.creationTimestamp }}
          </time>
          <div
            v-permission="['system:plugins:manage']"
            class="flex items-center"
          >
            <VSwitch
              :model-value="plugin?.spec.enabled"
              @click="changeStatus"
            />
          </div>
          <span v-permission="['system:plugins:manage']" class="cursor-pointer">
            <FloatingDropdown>
              <IconSettings />
              <template #popper>
                <div class="links-w-48 links-p-2">
                  <VSpace class="links-w-full" direction="column">
                    <VButton block type="danger" @click="uninstall">
                      卸载
                    </VButton>
                  </VSpace>
                </div>
              </template>
            </FloatingDropdown>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
