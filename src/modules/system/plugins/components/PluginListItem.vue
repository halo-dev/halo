<script lang="ts" setup>
import {
  VButton,
  VSpace,
  VSwitch,
  VTag,
  VStatusDot,
  VEntity,
  VEntityField,
  VAvatar,
} from "@halo-dev/components";
import PluginUploadModal from "./PluginUploadModal.vue";
import { ref, toRefs } from "vue";
import { usePluginLifeCycle } from "../composables/use-plugin";
import type { Plugin } from "@halo-dev/api-client";
import { formatDatetime } from "@/utils/date";
import { usePermission } from "@/utils/permission";

const { currentUserHasPermission } = usePermission();

const props = withDefaults(
  defineProps<{
    plugin?: Plugin;
  }>(),
  {
    plugin: undefined,
  }
);

const emit = defineEmits<{
  (event: "reload"): void;
}>();

const { plugin } = toRefs(props);

const upgradeModal = ref(false);

const { isStarted, changeStatus, uninstall } = usePluginLifeCycle(plugin);

const onUpgradeModalClose = () => {
  emit("reload");
};
</script>
<template>
  <PluginUploadModal
    v-model:visible="upgradeModal"
    :upgrade-plugin="plugin"
    @close="onUpgradeModalClose"
  />
  <VEntity>
    <template #start>
      <VEntityField>
        <template #description>
          <VAvatar
            :alt="plugin?.spec.displayName"
            :src="plugin?.status?.logo"
            size="md"
          ></VAvatar>
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
      <VEntityField v-if="plugin?.metadata.deletionTimestamp">
        <template #description>
          <VStatusDot v-tooltip="`删除中`" state="warning" animate />
        </template>
      </VEntityField>
      <VEntityField v-if="plugin?.spec.author">
        <template #description>
          <a
            :href="plugin?.spec.author.website"
            class="hidden text-sm text-gray-500 hover:text-gray-900 sm:block"
            target="_blank"
          >
            @{{ plugin?.spec.author.name }}
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
      <VEntityField v-permission="['system:plugins:manage']">
        <template #description>
          <div class="flex items-center">
            <VSwitch
              :model-value="plugin?.spec.enabled"
              @click="changeStatus"
            />
          </div>
        </template>
      </VEntityField>
    </template>
    <template
      v-if="currentUserHasPermission(['system:plugins:manage'])"
      #dropdownItems
    >
      <VButton
        v-close-popper
        block
        type="secondary"
        @click="upgradeModal = true"
      >
        升级
      </VButton>
      <FloatingDropdown class="w-full" placement="left" :triggers="['click']">
        <VButton block type="danger"> 卸载 </VButton>
        <template #popper>
          <div class="w-52 p-2">
            <VSpace class="w-full" direction="column">
              <VButton
                v-close-popper.all
                block
                type="danger"
                @click="uninstall"
              >
                卸载
              </VButton>
              <VButton
                v-close-popper.all
                block
                type="danger"
                @click="uninstall(true)"
              >
                卸载并删除配置
              </VButton>
            </VSpace>
          </div>
        </template>
      </FloatingDropdown>
    </template>
  </VEntity>
</template>
