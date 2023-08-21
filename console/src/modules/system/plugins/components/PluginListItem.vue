<script lang="ts" setup>
import {
  VSwitch,
  VStatusDot,
  VEntity,
  VEntityField,
  VAvatar,
  Dialog,
  Toast,
  VDropdownItem,
  VDropdown,
  VDropdownDivider,
} from "@halo-dev/components";
import PluginUploadModal from "./PluginUploadModal.vue";
import { ref, toRefs } from "vue";
import { usePluginLifeCycle } from "../composables/use-plugin";
import type { Plugin } from "@halo-dev/api-client";
import { formatDatetime } from "@/utils/date";
import { usePermission } from "@/utils/permission";
import { apiClient } from "@/utils/api-client";
import { useI18n } from "vue-i18n";

const { currentUserHasPermission } = usePermission();
const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    plugin?: Plugin;
  }>(),
  {
    plugin: undefined,
  }
);

const { plugin } = toRefs(props);

const upgradeModal = ref(false);

const { getFailedMessage, changeStatus, uninstall } =
  usePluginLifeCycle(plugin);

const handleResetSettingConfig = async () => {
  Dialog.warning({
    title: t("core.plugin.operations.reset.title"),
    description: t("core.plugin.operations.reset.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      try {
        if (!plugin?.value) {
          return;
        }

        await apiClient.plugin.resetPluginConfig({
          name: plugin.value.metadata.name as string,
        });

        Toast.success(t("core.plugin.operations.reset.toast_success"));
      } catch (e) {
        console.error("Failed to reset plugin setting config", e);
      }
    },
  });
};
</script>
<template>
  <PluginUploadModal v-model:visible="upgradeModal" :upgrade-plugin="plugin" />
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
      />
    </template>
    <template #end>
      <VEntityField v-if="plugin?.status?.phase === 'FAILED'">
        <template #description>
          <VStatusDot v-tooltip="getFailedMessage()" state="error" animate />
        </template>
      </VEntityField>
      <VEntityField v-if="plugin?.metadata.deletionTimestamp">
        <template #description>
          <VStatusDot
            v-tooltip="$t('core.common.status.deleting')"
            state="warning"
            animate
          />
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
      <VDropdownItem
        @click="
          $router.push({
            name: 'PluginDetail',
            params: { name: plugin?.metadata.name },
          })
        "
      >
        {{ $t("core.common.buttons.detail") }}
      </VDropdownItem>
      <VDropdownItem @click="upgradeModal = true">
        {{ $t("core.common.buttons.upgrade") }}
      </VDropdownItem>
      <VDropdownDivider />
      <VDropdown placement="left" :triggers="['click']">
        <VDropdownItem type="danger">
          {{ $t("core.common.buttons.uninstall") }}
        </VDropdownItem>
        <template #popper>
          <VDropdownItem type="danger" @click="uninstall">
            {{ $t("core.common.buttons.uninstall") }}
          </VDropdownItem>
          <VDropdownItem type="danger" @click="uninstall(true)">
            {{ $t("core.plugin.list.actions.uninstall_and_delete_config") }}
          </VDropdownItem>
        </template>
      </VDropdown>
      <VDropdownItem type="danger" @click="handleResetSettingConfig">
        {{ $t("core.common.buttons.reset") }}
      </VDropdownItem>
    </template>
  </VEntity>
</template>
