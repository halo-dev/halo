<script lang="ts" setup>
import { VModal, VButton, VTabbar } from "@halo-dev/components";
import type { Plugin } from "@halo-dev/api-client";
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRouteQuery } from "@vueuse/router";
import { provide } from "vue";
import { toRefs } from "vue";
import type { Ref } from "vue";
import LocalUpload from "./installation-tabs/LocalUpload.vue";
import RemoteDownload from "./installation-tabs/RemoteDownload.vue";
import { markRaw } from "vue";
import type {
  PluginInstallationTab,
  PluginModule,
} from "@halo-dev/console-shared";
import { usePluginModuleStore } from "@/stores/plugin";
import { onMounted } from "vue";

const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    visible: boolean;
    pluginToUpgrade?: Plugin;
  }>(),
  {
    visible: false,
    pluginToUpgrade: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const { pluginToUpgrade } = toRefs(props);
provide<Ref<Plugin | undefined>>("pluginToUpgrade", pluginToUpgrade);

const tabs = ref<PluginInstallationTab[]>([
  {
    id: "local",
    label: t("core.plugin.upload_modal.tabs.local"),
    component: markRaw(LocalUpload),
    priority: 10,
  },
  {
    id: "remote",
    label: t("core.plugin.upload_modal.tabs.remote.title"),
    component: markRaw(RemoteDownload),
    priority: 20,
  },
]);

const activeTabId = ref();

const modalTitle = computed(() => {
  return props.pluginToUpgrade
    ? t("core.plugin.upload_modal.titles.upgrade", {
        display_name: props.pluginToUpgrade.spec.displayName,
      })
    : t("core.plugin.upload_modal.titles.install");
});

const handleVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

// handle remote download url from route
const routeRemoteDownloadUrl = useRouteQuery<string | null>(
  "remote-download-url"
);

watch(
  () => props.visible,
  (visible) => {
    if (visible && routeRemoteDownloadUrl.value) {
      activeTabId.value = "remote";
    }
  }
);

const { pluginModules } = usePluginModuleStore();
onMounted(() => {
  pluginModules.forEach((pluginModule: PluginModule) => {
    const { extensionPoints } = pluginModule;
    if (!extensionPoints?.["plugin:installation:tabs:create"]) {
      return;
    }

    const items = extensionPoints[
      "plugin:installation:tabs:create"
    ]() as PluginInstallationTab[];

    tabs.value.push(...items);
  });

  tabs.value.sort((a, b) => {
    return a.priority - b.priority;
  });

  activeTabId.value = tabs.value[0].id;
});
</script>
<template>
  <VModal
    :visible="visible"
    :title="modalTitle"
    :centered="true"
    :width="920"
    height="calc(100vh - 20px)"
    @update:visible="handleVisibleChange"
  >
    <VTabbar
      v-model:active-id="activeTabId"
      :items="
        tabs.map((tab) => {
          return { label: tab.label, id: tab.id };
        })
      "
      type="outline"
    />
    <div class="mt-2">
      <template v-for="tab in tabs" :key="tab.id">
        <component
          :is="tab.component"
          v-bind="tab.props"
          v-if="tab.id === activeTabId"
          @close-modal="handleVisibleChange(false)"
        />
      </template>
    </div>
    <template #footer>
      <VButton @click="handleVisibleChange(false)">
        {{ $t("core.common.buttons.close") }}
      </VButton>
    </template>
  </VModal>
</template>
