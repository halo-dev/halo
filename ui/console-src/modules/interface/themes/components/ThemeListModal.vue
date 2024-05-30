<script lang="ts" setup>
import { usePluginModuleStore } from "@/stores/plugin";
import { usePermission } from "@/utils/permission";
import type { Theme } from "@halo-dev/api-client";
import { VButton, VModal, VTabbar } from "@halo-dev/components";
import type { ThemeListTab } from "@halo-dev/console-shared";
import { useRouteQuery } from "@vueuse/router";
import {
  computed,
  inject,
  markRaw,
  nextTick,
  onMounted,
  provide,
  ref,
  watch,
  type Ref,
} from "vue";
import { useI18n } from "vue-i18n";
import InstalledThemes from "./list-tabs/InstalledThemes.vue";
import LocalUpload from "./list-tabs/LocalUpload.vue";
import NotInstalledThemes from "./list-tabs/NotInstalledThemes.vue";
import RemoteDownload from "./list-tabs/RemoteDownload.vue";

const { t } = useI18n();
const { currentUserHasPermission } = usePermission();

const selectedTheme = inject<Ref<Theme | undefined>>("selectedTheme", ref());

const emit = defineEmits<{
  (event: "close"): void;
  (event: "select", theme: Theme | undefined): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const tabs = ref<ThemeListTab[]>([
  {
    id: "installed",
    label: t("core.theme.list_modal.tabs.installed"),
    component: markRaw(InstalledThemes),
    priority: 10,
  },
  {
    id: "local-upload",
    label: t("core.theme.list_modal.tabs.local_upload"),
    component: markRaw(LocalUpload),
    priority: 20,
  },
  {
    id: "remote-download",
    label: t("core.theme.list_modal.tabs.remote_download.label"),
    component: markRaw(RemoteDownload),
    priority: 30,
  },
  {
    id: "not_installed",
    label: t("core.theme.list_modal.tabs.not_installed"),
    component: markRaw(NotInstalledThemes),
    priority: 40,
  },
]);

watch(
  () => selectedTheme.value,
  (value, oldValue) => {
    if (value && oldValue) {
      emit("select", value);
      modal.value?.close();
    }
  }
);

const activeTabId = ref();

provide<Ref<string>>("activeTabId", activeTabId);

const modalTitle = computed(() => {
  const tab = tabs.value.find((tab) => tab.id === activeTabId.value);
  return tab?.label;
});

// handle remote wordpress url from route
const remoteDownloadUrl = useRouteQuery<string>("remote-download-url");

onMounted(() => {
  if (remoteDownloadUrl.value) {
    nextTick(() => {
      activeTabId.value = "remote-download";
    });
  }
});

const { pluginModules } = usePluginModuleStore();

onMounted(async () => {
  const tabsFromPlugins: ThemeListTab[] = [];

  for (const pluginModule of pluginModules) {
    try {
      const callbackFunction =
        pluginModule?.extensionPoints?.["theme:list:tabs:create"];

      if (typeof callbackFunction !== "function") {
        continue;
      }

      const items = await callbackFunction();

      tabsFromPlugins.push(
        ...items.filter((item) => {
          return currentUserHasPermission(item.permissions);
        })
      );
    } catch (error) {
      console.error(`Error processing plugin module:`, pluginModule, error);
    }
  }

  tabs.value = tabs.value.concat(tabsFromPlugins).sort((a, b) => {
    return a.priority - b.priority;
  });

  activeTabId.value = tabs.value[0].id;
});
</script>
<template>
  <VModal
    ref="modal"
    :width="920"
    height="calc(100vh - 20px)"
    :title="modalTitle"
    @close="emit('close')"
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
        />
      </template>
    </div>

    <template #footer>
      <VButton @click="modal?.close()">
        {{ $t("core.common.buttons.close") }}
      </VButton>
    </template>
  </VModal>
</template>
