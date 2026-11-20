<script lang="ts" setup>
import { useOperationItemExtensionPoint } from "@console/composables/use-operation-extension-points";
import PluginInstallationModal from "@console/modules/system/plugins/components/PluginInstallationModal.vue";
import { usePluginLifeCycle } from "@console/modules/system/plugins/composables/use-plugin";
import type { Plugin } from "@halo-dev/api-client";
import {
  IconMore,
  VDropdown,
  VDropdownDivider,
  VDropdownItem,
} from "@halo-dev/components";
import { utils, type OperationItem } from "@halo-dev/ui-shared";
import type { Ref } from "vue";
import { computed, markRaw, ref, toRefs } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import EntityDropdownItems from "@/components/entity/EntityDropdownItems.vue";
import { pluginLabels } from "@/constants/labels";

const { t } = useI18n();
const router = useRouter();

const props = defineProps<{ plugin: Plugin }>();

const { plugin } = toRefs(props);

const { uninstall, reload, resetPluginConfig } = usePluginLifeCycle(
  plugin as Ref<Plugin>
);

const pluginUpgradeModalVisible = ref(false);

const { data: operationItems } = useOperationItemExtensionPoint<Plugin>(
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
          params: { name: props.plugin.metadata.name },
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
      hidden:
        plugin.value.metadata.labels?.[pluginLabels.SYSTEM_RESERVED] === "true",
    },
    {
      priority: 30,
      component: markRaw(VDropdownDivider),
    },
    {
      priority: 40,
      component: markRaw(VDropdownItem),
      props: { type: "danger" },
      label: t("core.common.buttons.uninstall"),
      children: [
        {
          priority: 10,
          component: markRaw(VDropdownItem),
          props: { type: "danger" },
          label: t("core.common.buttons.uninstall"),
          action: () =>
            uninstall({
              deleteExtensions: false,
              onSuccess: () => window.location.reload(),
            }),
        },
        {
          priority: 20,
          component: markRaw(VDropdownItem),
          props: { type: "danger" },
          label: t("core.plugin.operations.uninstall_and_delete_config.button"),
          action: () =>
            uninstall({
              deleteExtensions: true,
              onSuccess: () => window.location.reload(),
            }),
        },
      ],
      hidden:
        plugin.value.metadata.labels?.[pluginLabels.SYSTEM_RESERVED] === "true",
    },
    {
      priority: 50,
      component: markRaw(VDropdownItem),
      props: { type: "danger" },
      label: t("core.plugin.operations.reload.button"),
      hidden: !plugin.value.spec.enabled,
      action: reload,
    },
    {
      priority: 60,
      component: markRaw(VDropdownItem),
      props: { type: "danger" },
      label: t("core.plugin.operations.reset.button"),
      action: resetPluginConfig,
    },
  ])
);
</script>

<template>
  <VDropdown v-if="utils.permission.has(['system:plugins:manage'])">
    <button
      aria-label="更多操作"
      class="inline-flex cursor-pointer rounded p-1 text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-900"
      type="button"
    >
      <IconMore />
    </button>
    <template #popper>
      <EntityDropdownItems
        :dropdown-items="operationItems || []"
        :item="plugin"
      />
    </template>
  </VDropdown>

  <PluginInstallationModal
    v-if="
      pluginUpgradeModalVisible &&
      utils.permission.has(['system:plugins:manage'])
    "
    :plugin-to-upgrade="plugin"
    @close="pluginUpgradeModalVisible = false"
  />
</template>
