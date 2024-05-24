<script lang="ts" setup>
import {
  Dialog,
  Toast,
  VDropdownDivider,
  VDropdownItem,
  VEntity,
  VEntityField,
} from "@halo-dev/components";
import type { Ref } from "vue";
import { computed, inject, markRaw, ref, toRefs } from "vue";
import { usePluginLifeCycle } from "../composables/use-plugin";
import { type Plugin, PluginStatusPhaseEnum } from "@halo-dev/api-client";
import { formatDatetime } from "@/utils/date";
import { usePermission } from "@/utils/permission";
import { apiClient } from "@/utils/api-client";
import { useI18n } from "vue-i18n";
import { useEntityFieldItemExtensionPoint } from "@console/composables/use-entity-extension-points";
import { useOperationItemExtensionPoint } from "@console/composables/use-operation-extension-points";
import { useRouter } from "vue-router";
import EntityDropdownItems from "@/components/entity/EntityDropdownItems.vue";
import EntityFieldItems from "@/components/entity-fields/EntityFieldItems.vue";
import LogoField from "./entity-fields/LogoField.vue";
import StatusDotField from "@/components/entity-fields/StatusDotField.vue";
import AuthorField from "./entity-fields/AuthorField.vue";
import SwitchField from "./entity-fields/SwitchField.vue";
import type { EntityFieldItem, OperationItem } from "@halo-dev/console-shared";
import PluginInstallationModal from "@console/modules/system/plugins/components/PluginInstallationModal.vue";

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

const { plugin } = toRefs(props);

const selectedNames = inject<Ref<string[]>>("selectedNames", ref([]));

const { getStatusDotState, getStatusMessage, uninstall } =
  usePluginLifeCycle(plugin);

const pluginUpgradeModalVisible = ref(false);

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

const { operationItems } = useOperationItemExtensionPoint<Plugin>(
  "plugin:list-item:operation:create",
  plugin,
  computed((): OperationItem<Plugin>[] => [
    {
      priority: 10,
      component: markRaw(VDropdownItem),
      label: t("core.common.buttons.detail"),
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
      permissions: [],
      action: () => {
        pluginUpgradeModalVisible.value = true;
      },
    },
    {
      priority: 30,
      component: markRaw(VDropdownDivider),
    },
    {
      priority: 40,
      component: markRaw(VDropdownItem),
      props: {
        type: "danger",
      },
      label: t("core.common.buttons.uninstall"),
      children: [
        {
          priority: 10,
          component: markRaw(VDropdownItem),
          props: {
            type: "danger",
          },
          label: t("core.common.buttons.uninstall"),
          action: () => uninstall(),
        },
        {
          priority: 20,
          component: markRaw(VDropdownItem),
          props: {
            type: "danger",
          },
          label: t("core.plugin.operations.uninstall_and_delete_config.button"),
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
      action: () => {
        handleResetSettingConfig();
      },
    },
  ])
);

const { startFields, endFields } = useEntityFieldItemExtensionPoint<Plugin>(
  "plugin:list-item:field:create",
  plugin,
  computed((): EntityFieldItem[] => {
    const { phase } = props.plugin.status || {};

    const shouldHideStatusDot =
      phase === PluginStatusPhaseEnum.Started ||
      phase === PluginStatusPhaseEnum.Disabled;

    return [
      {
        position: "start",
        priority: 10,
        component: markRaw(LogoField),
        props: {
          plugin: props.plugin,
        },
      },
      {
        position: "start",
        priority: 20,
        component: markRaw(VEntityField),
        props: {
          title: props.plugin.spec.displayName,
          description: props.plugin.spec.description,
          route: {
            name: "PluginDetail",
            params: { name: props.plugin.metadata.name },
          },
        },
      },
      {
        position: "end",
        priority: 10,
        component: markRaw(StatusDotField),
        props: {
          tooltip: getStatusMessage(),
          state: getStatusDotState(),
          animate: true,
        },
        hidden: shouldHideStatusDot,
      },
      {
        position: "end",
        priority: 20,
        component: markRaw(StatusDotField),
        props: {
          tooltip: t("core.common.status.deleting"),
          state: "warning",
          animate: true,
        },
        hidden: !props.plugin.metadata.deletionTimestamp,
      },
      {
        position: "end",
        priority: 30,
        component: markRaw(AuthorField),
        props: {
          plugin: props.plugin,
        },
        hidden: !props.plugin.spec.author,
      },
      {
        position: "end",
        priority: 40,
        component: markRaw(VEntityField),
        props: {
          description: props.plugin.spec.version,
        },
      },
      {
        position: "end",
        priority: 50,
        component: markRaw(VEntityField),
        props: {
          description: formatDatetime(props.plugin.metadata.creationTimestamp),
        },
        hidden: !props.plugin.metadata.creationTimestamp,
      },
      {
        position: "end",
        priority: 60,
        component: markRaw(SwitchField),
        props: {
          plugin: props.plugin,
        },
        permissions: ["system:plugins:manage"],
      },
    ];
  })
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
        name="post-checkbox"
        type="checkbox"
      />
    </template>
    <template #start>
      <EntityFieldItems :fields="startFields" />
    </template>
    <template #end>
      <EntityFieldItems :fields="endFields" />
    </template>
    <template
      v-if="currentUserHasPermission(['system:plugins:manage'])"
      #dropdownItems
    >
      <EntityDropdownItems :dropdown-items="operationItems" :item="plugin" />
    </template>
  </VEntity>

  <PluginInstallationModal
    v-if="
      pluginUpgradeModalVisible &&
      currentUserHasPermission(['system:plugins:manage'])
    "
    :plugin-to-upgrade="plugin"
    @close="pluginUpgradeModalVisible = false"
  />
</template>
