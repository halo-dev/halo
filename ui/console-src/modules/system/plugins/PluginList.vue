<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import { usePermission } from "@/utils/permission";
import { PluginStatusPhaseEnum, type Plugin } from "@halo-dev/api-client";
import {
  Dialog,
  IconAddCircle,
  IconPlug,
  IconRefreshLine,
  VButton,
  VCard,
  VDropdown,
  VDropdownItem,
  VEmpty,
  VLoading,
  VPageHeader,
  VSpace,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { useRouteQuery } from "@vueuse/router";
import type { Ref } from "vue";
import { computed, onMounted, provide, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import PluginInstallationModal from "./components/PluginInstallationModal.vue";
import PluginListItem from "./components/PluginListItem.vue";
import { usePluginBatchOperations } from "./composables/use-plugin";

const { t } = useI18n();
const { currentUserHasPermission } = usePermission();

const pluginInstallationModalVisible = ref(false);

const keyword = useRouteQuery<string>("keyword", "");

const selectedEnabledValue = useRouteQuery<
  string | undefined,
  boolean | undefined
>("enabled", undefined, {
  transform: (value) => {
    return value ? value === "true" : undefined;
  },
});
const selectedSortValue = useRouteQuery<string | undefined>("sort");

const hasFilters = computed(() => {
  return selectedEnabledValue.value !== undefined || selectedSortValue.value;
});

function handleClearFilters() {
  selectedSortValue.value = undefined;
  selectedEnabledValue.value = undefined;
}

const total = ref(0);

const { data, isLoading, isFetching, refetch } = useQuery<Plugin[]>({
  queryKey: ["plugins", keyword, selectedEnabledValue, selectedSortValue],
  queryFn: async () => {
    const { data } = await apiClient.plugin.listPlugins({
      page: 0,
      size: 0,
      keyword: keyword.value,
      enabled: selectedEnabledValue.value,
      sort: [selectedSortValue.value].filter(Boolean) as string[],
    });

    total.value = data.total;

    return data.items;
  },
  keepPreviousData: true,
  refetchInterval: (data) => {
    const hasDeletingData = data?.some(
      (plugin) => !!plugin.metadata.deletionTimestamp
    );

    if (hasDeletingData) {
      return 1000;
    }

    const hasStartingData = data?.some(
      (plugin) =>
        plugin.spec.enabled &&
        plugin.status?.phase !==
          (PluginStatusPhaseEnum.Started || PluginStatusPhaseEnum.Failed)
    );

    if (hasStartingData) {
      return 3000;
    }

    return false;
  },
});

// selection
const selectedNames = ref<string[]>([]);
provide<Ref<string[]>>("selectedNames", selectedNames);
const checkedAll = ref(false);

watch(
  () => selectedNames.value,
  (value) => {
    checkedAll.value = value.length === data.value?.length;
  }
);

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;
  if (checked) {
    selectedNames.value =
      data.value?.map((plugin) => {
        return plugin.metadata.name;
      }) || [];
  } else {
    selectedNames.value.length = 0;
  }
};

const { handleChangeStatusInBatch, handleUninstallInBatch } =
  usePluginBatchOperations(selectedNames);

// handle remote download url from route
const routeRemoteDownloadUrl = useRouteQuery<string | null>(
  "remote-download-url"
);
onMounted(() => {
  if (routeRemoteDownloadUrl.value) {
    Dialog.warning({
      title: t("core.plugin.operations.remote_download.title"),
      description: t("core.plugin.operations.remote_download.description", {
        url: routeRemoteDownloadUrl.value,
      }),
      confirmText: t("core.common.buttons.download"),
      cancelText: t("core.common.buttons.cancel"),
      onConfirm() {
        pluginInstallationModalVisible.value = true;
      },
      onCancel() {
        routeRemoteDownloadUrl.value = null;
      },
    });
  }
});
</script>
<template>
  <PluginInstallationModal
    v-if="
      pluginInstallationModalVisible &&
      currentUserHasPermission(['system:plugins:manage'])
    "
    @close="pluginInstallationModalVisible = false"
  />

  <VPageHeader :title="$t('core.plugin.title')">
    <template #icon>
      <IconPlug class="mr-2 self-center" />
    </template>
    <template #actions>
      <VButton
        v-permission="['system:plugins:manage']"
        type="secondary"
        @click="pluginInstallationModalVisible = true"
      >
        <template #icon>
          <IconAddCircle class="h-full w-full" />
        </template>
        {{ $t("core.common.buttons.install") }}
      </VButton>
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <template #header>
        <div class="block w-full bg-gray-50 px-4 py-3">
          <div
            class="relative flex flex-col flex-wrap items-start gap-4 sm:flex-row sm:items-center"
          >
            <div
              v-permission="['system:posts:manage']"
              class="hidden items-center sm:flex"
            >
              <input
                v-model="checkedAll"
                type="checkbox"
                @change="handleCheckAllChange"
              />
            </div>
            <div class="flex w-full flex-1 items-center gap-2 sm:w-auto">
              <SearchInput v-if="!selectedNames.length" v-model="keyword" />
              <VSpace v-else>
                <VButton @click="handleChangeStatusInBatch(true)">
                  {{ $t("core.common.buttons.activate") }}
                </VButton>
                <VButton @click="handleChangeStatusInBatch(false)">
                  {{ $t("core.common.buttons.inactivate") }}
                </VButton>
                <VDropdown>
                  <VButton type="danger">
                    {{ $t("core.common.buttons.uninstall") }}
                  </VButton>
                  <template #popper>
                    <VDropdownItem
                      type="danger"
                      @click="handleUninstallInBatch(false)"
                    >
                      {{ $t("core.common.buttons.uninstall") }}
                    </VDropdownItem>
                    <VDropdownItem
                      type="danger"
                      @click="handleUninstallInBatch(true)"
                    >
                      {{
                        $t(
                          "core.plugin.operations.uninstall_and_delete_config.button"
                        )
                      }}
                    </VDropdownItem>
                  </template>
                </VDropdown>
              </VSpace>
            </div>
            <VSpace spacing="lg" class="flex-wrap">
              <FilterCleanButton
                v-if="hasFilters"
                @click="handleClearFilters"
              />
              <FilterDropdown
                v-model="selectedEnabledValue"
                :label="$t('core.common.filters.labels.status')"
                :items="[
                  {
                    label: t('core.common.filters.item_labels.all'),
                  },
                  {
                    label: t('core.plugin.filters.status.items.active'),
                    value: true,
                  },
                  {
                    label: t('core.plugin.filters.status.items.inactive'),
                    value: false,
                  },
                ]"
              />
              <FilterDropdown
                v-model="selectedSortValue"
                :label="$t('core.common.filters.labels.sort')"
                :items="[
                  {
                    label: t('core.common.filters.item_labels.default'),
                  },
                  {
                    label: t('core.plugin.filters.sort.items.create_time_desc'),
                    value: 'creationTimestamp,desc',
                  },
                  {
                    label: t('core.plugin.filters.sort.items.create_time_asc'),
                    value: 'creationTimestamp,asc',
                  },
                ]"
              />
              <div class="flex flex-row gap-2">
                <div
                  class="group cursor-pointer rounded p-1 hover:bg-gray-200"
                  @click="refetch()"
                >
                  <IconRefreshLine
                    v-tooltip="$t('core.common.buttons.refresh')"
                    :class="{ 'animate-spin text-gray-900': isFetching }"
                    class="h-4 w-4 text-gray-600 group-hover:text-gray-900"
                  />
                </div>
              </div>
            </VSpace>
          </div>
        </div>
      </template>

      <VLoading v-if="isLoading" />

      <Transition v-else-if="!data?.length" appear name="fade">
        <VEmpty
          :message="$t('core.plugin.empty.message')"
          :title="$t('core.plugin.empty.title')"
        >
          <template #actions>
            <VSpace>
              <VButton :loading="isFetching" @click="refetch()">
                {{ $t("core.common.buttons.refresh") }}
              </VButton>
              <VButton
                v-permission="['system:plugins:manage']"
                type="secondary"
                @click="pluginInstallationModalVisible = true"
              >
                <template #icon>
                  <IconAddCircle class="h-full w-full" />
                </template>
                {{ $t("core.plugin.empty.actions.install") }}
              </VButton>
            </VSpace>
          </template>
        </VEmpty>
      </Transition>

      <Transition v-else appear name="fade">
        <ul
          class="box-border h-full w-full divide-y divide-gray-100"
          role="list"
        >
          <li v-for="plugin in data" :key="plugin.metadata.name">
            <PluginListItem
              :plugin="plugin"
              :is-selected="selectedNames.includes(plugin.metadata.name)"
            />
          </li>
        </ul>
      </Transition>

      <template #footer>
        <div class="flex h-8 items-center">
          <span class="text-sm text-gray-500">
            {{ $t("core.components.pagination.total_label", { total: total }) }}
          </span>
        </div>
      </template>
    </VCard>
  </div>
</template>
