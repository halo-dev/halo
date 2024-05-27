<script lang="ts" setup>
import { VButton, VModal, VTabbar } from "@halo-dev/components";
import type { Plugin } from "@halo-dev/api-client";
import {
  computed,
  markRaw,
  nextTick,
  onMounted,
  provide,
  type Ref,
  ref,
  toRefs,
} from "vue";
import { useI18n } from "vue-i18n";
import { useRouteQuery } from "@vueuse/router";
import LocalUpload from "./installation-tabs/LocalUpload.vue";
import RemoteDownload from "./installation-tabs/RemoteDownload.vue";
import type {
  PluginInstallationTab,
  PluginModule,
} from "@halo-dev/console-shared";
import { usePluginModuleStore } from "@/stores/plugin";
import { usePermission } from "@/utils/permission";

const { t } = useI18n();
const { currentUserHasPermission } = usePermission();

const props = withDefaults(
  defineProps<{
    pluginToUpgrade?: Plugin;
  }>(),
  {
    pluginToUpgrade: undefined,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const { pluginToUpgrade } = toRefs(props);
provide<Ref<Plugin | undefined>>("pluginToUpgrade", pluginToUpgrade);

const modal = ref<InstanceType<typeof VModal> | null>(null);

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

// handle remote download url from route
const routeRemoteDownloadUrl = useRouteQuery<string | null>(
  "remote-download-url"
);

onMounted(() => {
  if (routeRemoteDownloadUrl.value) {
    nextTick(() => {
      activeTabId.value = "remote";
    });
  }
});

const { pluginModules } = usePluginModuleStore();
onMounted(() => {
  pluginModules.forEach((pluginModule: PluginModule) => {
    const { extensionPoints } = pluginModule;
    if (!extensionPoints?.["plugin:installation:tabs:create"]) {
      return;
    }

    let items = extensionPoints[
      "plugin:installation:tabs:create"
    ]() as PluginInstallationTab[];

    items = items.filter((item) => {
      return currentUserHasPermission(item.permissions);
    });

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
    ref="modal"
    :title="modalTitle"
    :centered="true"
    :width="920"
    height="calc(100vh - 20px)"
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
          @close-modal="modal?.close()"
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
