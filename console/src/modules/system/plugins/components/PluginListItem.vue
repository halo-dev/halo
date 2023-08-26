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
  VDropdownDivider,
} from "@halo-dev/components";
import { inject, toRefs, markRaw } from "vue";
import { usePluginLifeCycle } from "../composables/use-plugin";
import type { Plugin } from "@halo-dev/api-client";
import { formatDatetime } from "@/utils/date";
import { usePermission } from "@/utils/permission";
import { apiClient } from "@/utils/api-client";
import { useI18n } from "vue-i18n";
import type { Ref } from "vue";
import { ref } from "vue";
import { useEntityDropdownItemExtensionPoint } from "@/composables/use-entity-extension-points";
import { useRouter } from "vue-router";
import EntityDropdownItems from "@/components/entity/EntityDropdownItems.vue";

const { currentUserHasPermission } = usePermission();
const { t } = useI18n();
const router = useRouter();

const props = withDefaults(
  defineProps<{
    plugin: Plugin;
    isSelected?: boolean;
  }>(),
  { isSelected: false }
);

const emit = defineEmits<{
  (event: "open-upgrade-modal", plugin?: Plugin): void;
}>();

const { plugin } = toRefs(props);

const selectedNames = inject<Ref<string[]>>("selectedNames", ref([]));

const { getFailedMessage, changeStatus, changingStatus, uninstall } =
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

const { dropdownItems } = useEntityDropdownItemExtensionPoint<Plugin>(
  "plugin:list-item:operation:create",
  [
    {
      priority: 10,
      component: markRaw(VDropdownItem),
      label: t("core.common.buttons.detail"),
      visible: true,
      permissions: [],
      action: () => {
        router.push({
          name: "PluginDetail",
          params: { name: props.plugin?.metadata.name },
        });
      },
    },
    {
      priority: 20,
      component: markRaw(VDropdownItem),
      label: t("core.common.buttons.upgrade"),
      visible: true,
      permissions: [],
      action: () => {
        emit("open-upgrade-modal", props.plugin);
      },
    },
    {
      priority: 30,
      component: markRaw(VDropdownDivider),
      visible: true,
    },
    {
      priority: 40,
      component: markRaw(VDropdownItem),
      props: {
        type: "danger",
      },
      label: t("core.common.buttons.uninstall"),
      visible: true,
      children: [
        {
          priority: 10,
          component: markRaw(VDropdownItem),
          props: {
            type: "danger",
          },
          label: t("core.common.buttons.uninstall"),
          visible: true,
          action: () => uninstall(),
        },
        {
          priority: 20,
          component: markRaw(VDropdownItem),
          props: {
            type: "danger",
          },
          label: t("core.plugin.operations.uninstall_and_delete_config.button"),
          visible: true,
          action: () => uninstall(true),
        },
      ],
    },
    {
      priority: 50,
      component: markRaw(VDropdownItem),
      props: {
        type: "danger",
      },
      label: t("core.common.buttons.reset"),
      visible: true,
      action: () => {
        handleResetSettingConfig();
      },
    },
  ]
);
</script>
<template>
  <VEntity :is-selected="isSelected">
    <template
      v-if="currentUserHasPermission(['system:plugins:manage'])"
      #checkbox
    >
      <input
        v-model="selectedNames"
        :value="plugin.metadata.name"
        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
        name="post-checkbox"
        type="checkbox"
      />
    </template>
    <template #start>
      <VEntityField>
        <template #description>
          <VAvatar
            :alt="plugin.spec.displayName"
            :src="plugin.status?.logo"
            size="md"
          ></VAvatar>
        </template>
      </VEntityField>
      <VEntityField
        :title="plugin.spec.displayName"
        :description="plugin.spec.description"
        :route="{
          name: 'PluginDetail',
          params: { name: plugin.metadata.name },
        }"
      />
    </template>
    <template #end>
      <VEntityField v-if="plugin.status?.phase === 'FAILED'">
        <template #description>
          <VStatusDot v-tooltip="getFailedMessage()" state="error" animate />
        </template>
      </VEntityField>
      <VEntityField v-if="plugin.metadata.deletionTimestamp">
        <template #description>
          <VStatusDot
            v-tooltip="$t('core.common.status.deleting')"
            state="warning"
            animate
          />
        </template>
      </VEntityField>
      <VEntityField v-if="plugin.spec.author">
        <template #description>
          <a
            :href="plugin.spec.author.website"
            class="hidden text-sm text-gray-500 hover:text-gray-900 sm:block"
            target="_blank"
          >
            @{{ plugin.spec.author.name }}
          </a>
        </template>
      </VEntityField>
      <VEntityField :description="plugin.spec.version" />
      <VEntityField v-if="plugin.metadata.creationTimestamp">
        <template #description>
          <span class="truncate text-xs tabular-nums text-gray-500">
            {{ formatDatetime(plugin.metadata.creationTimestamp) }}
          </span>
        </template>
      </VEntityField>
      <VEntityField v-permission="['system:plugins:manage']">
        <template #description>
          <div class="flex items-center">
            <VSwitch
              :model-value="plugin.spec.enabled"
              :disabled="changingStatus"
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
      <EntityDropdownItems :dropdown-items="dropdownItems" :item="plugin" />
    </template>
  </VEntity>
</template>
