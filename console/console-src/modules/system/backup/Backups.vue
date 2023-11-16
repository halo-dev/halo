<script lang="ts" setup>
import {
  VPageHeader,
  VCard,
  VButton,
  VTabbar,
  IconAddCircle,
  IconServerLine,
} from "@halo-dev/components";

import { onMounted, shallowRef } from "vue";
import ListTab from "./tabs/List.vue";
import RestoreTab from "./tabs/Restore.vue";
import { useRouteQuery } from "@vueuse/router";
import { markRaw } from "vue";
import { useI18n } from "vue-i18n";
import { useBackup } from "./composables/use-backup";
import { usePluginModuleStore } from "@/stores/plugin";
import type { BackupTab } from "@halo-dev/console-shared";

const { t } = useI18n();

const tabs = shallowRef<BackupTab[]>([
  {
    id: "backups",
    label: t("core.backup.tabs.backup_list"),
    component: markRaw(ListTab),
  },
  {
    id: "restore",
    label: t("core.backup.tabs.restore"),
    component: markRaw(RestoreTab),
  },
]);

const activeTab = useRouteQuery<string>("tab", tabs.value[0].id);

const { handleCreate } = useBackup();

onMounted(() => {
  const { pluginModules } = usePluginModuleStore();

  pluginModules.forEach((pluginModule) => {
    const { extensionPoints } = pluginModule;
    if (!extensionPoints?.["backup:tabs:create"]) {
      return;
    }

    const backupTabs = extensionPoints["backup:tabs:create"]() as BackupTab[];

    if (backupTabs) {
      tabs.value = tabs.value.concat(backupTabs);
    }
  });
});
</script>

<template>
  <VPageHeader :title="$t('core.backup.title')">
    <template #icon>
      <IconServerLine class="mr-2 self-center" />
    </template>
    <template #actions>
      <VButton type="secondary" @click="handleCreate">
        <template #icon>
          <IconAddCircle class="h-full w-full" />
        </template>
        {{ $t("core.backup.operations.create.button") }}
      </VButton>
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <template #header>
        <VTabbar
          v-model:active-id="activeTab"
          :items="tabs.map((item) => ({ id: item.id, label: item.label }))"
          class="w-full !rounded-none"
          type="outline"
        ></VTabbar>
      </template>
      <div class="bg-white">
        <template v-for="tab in tabs" :key="tab.id">
          <component :is="tab.component" v-if="activeTab === tab.id" />
        </template>
      </div>
    </VCard>
  </div>
</template>
