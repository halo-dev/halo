<script lang="ts" setup>
import { usePluginModuleStore } from "@/stores/plugin";
import type { Plugin } from "@halo-dev/api-client";
import { VButton, VLoading, VModal, VTabbar } from "@halo-dev/components";
import { utils, type PluginInstallationTab } from "@halo-dev/ui-shared";
import { useRouteQuery } from "@vueuse/router";
import {
  computed,
  defineAsyncComponent,
  nextTick,
  onMounted,
  provide,
  ref,
  shallowRef,
  toRefs,
  type Ref,
} from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

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

const tabs = shallowRef<PluginInstallationTab[]>([
  {
    id: "local",
    label: t("core.plugin.upload_modal.tabs.local"),
    component: defineAsyncComponent({
      loader: () => import("./installation-tabs/LocalUpload.vue"),
      loadingComponent: VLoading,
    }),
    priority: 10,
  },
  {
    id: "remote",
    label: t("core.plugin.upload_modal.tabs.remote.title"),
    component: defineAsyncComponent({
      loader: () => import("./installation-tabs/RemoteDownload.vue"),
      loadingComponent: VLoading,
    }),
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

onMounted(async () => {
  for (const pluginModule of pluginModules) {
    try {
      const callbackFunction =
        pluginModule?.extensionPoints?.["plugin:installation:tabs:create"];

      if (typeof callbackFunction !== "function") {
        continue;
      }

      const items = await callbackFunction();

      tabs.value.push(
        ...items.filter((item) => {
          return utils.permission.has(item.permissions || []);
        })
      );
    } catch (error) {
      console.error(`Error processing plugin module:`, pluginModule, error);
    }
  }

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
