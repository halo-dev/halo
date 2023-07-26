<script lang="ts" setup>
import {
  IconAddCircle,
  IconPlug,
  IconRefreshLine,
  VButton,
  VCard,
  VEmpty,
  VPageHeader,
  VPagination,
  VSpace,
  VLoading,
  Dialog,
} from "@halo-dev/components";
import PluginListItem from "./components/PluginListItem.vue";
import PluginUploadModal from "./components/PluginUploadModal.vue";
import { computed, ref, onMounted } from "vue";
import { apiClient } from "@/utils/api-client";
import { usePermission } from "@/utils/permission";
import { useQuery } from "@tanstack/vue-query";
import type { Plugin } from "@halo-dev/api-client";
import { useI18n } from "vue-i18n";
import { useRouteQuery } from "@vueuse/router";
import { watch } from "vue";

const { t } = useI18n();

const { currentUserHasPermission } = usePermission();

const pluginInstall = ref(false);

const keyword = ref("");
const page = ref(1);
const size = ref(20);
const total = ref(0);

const selectedEnabledValue = ref();
const selectedSortValue = ref();

const hasFilters = computed(() => {
  return selectedEnabledValue.value !== undefined || selectedSortValue.value;
});

function handleClearFilters() {
  selectedSortValue.value = undefined;
  selectedEnabledValue.value = undefined;
}

watch(
  () => [selectedEnabledValue.value, selectedSortValue.value, keyword.value],
  () => {
    page.value = 1;
  }
);

const { data, isLoading, isFetching, refetch } = useQuery<Plugin[]>({
  queryKey: [
    "plugins",
    page,
    size,
    keyword,
    selectedEnabledValue,
    selectedSortValue,
  ],
  queryFn: async () => {
    const { data } = await apiClient.plugin.listPlugins({
      page: page.value,
      size: size.value,
      keyword: keyword.value,
      enabled: selectedEnabledValue.value,
      sort: [selectedSortValue.value].filter(Boolean) as string[],
    });

    total.value = data.total;

    return data.items;
  },
  keepPreviousData: true,
  refetchInterval: (data) => {
    const deletingPlugins = data?.filter(
      (plugin) => !!plugin.metadata.deletionTimestamp
    );

    return deletingPlugins?.length ? 2000 : false;
  },
});

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
        pluginInstall.value = true;
      },
      onCancel() {
        routeRemoteDownloadUrl.value = null;
      },
    });
  }
});
</script>
<template>
  <PluginUploadModal
    v-if="currentUserHasPermission(['system:plugins:manage'])"
    v-model:visible="pluginInstall"
  />

  <VPageHeader :title="$t('core.plugin.title')">
    <template #icon>
      <IconPlug class="mr-2 self-center" />
    </template>
    <template #actions>
      <VButton
        v-permission="['system:plugins:manage']"
        type="secondary"
        @click="pluginInstall = true"
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
            class="relative flex flex-col items-start sm:flex-row sm:items-center"
          >
            <div class="flex w-full flex-1 items-center gap-2 sm:w-auto">
              <SearchInput v-model="keyword" />
            </div>
            <div class="mt-4 flex sm:mt-0">
              <VSpace spacing="lg">
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
                      label: t(
                        'core.plugin.filters.sort.items.create_time_desc'
                      ),
                      value: 'creationTimestamp,desc',
                    },
                    {
                      label: t(
                        'core.plugin.filters.sort.items.create_time_asc'
                      ),
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
                @click="pluginInstall = true"
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
            <PluginListItem :plugin="plugin" />
          </li>
        </ul>
      </Transition>

      <template #footer>
        <VPagination
          v-model:page="page"
          v-model:size="size"
          :page-label="$t('core.components.pagination.page_label')"
          :size-label="$t('core.components.pagination.size_label')"
          :total-label="
            $t('core.components.pagination.total_label', { total: total })
          "
          :total="total"
          :size-options="[10, 20, 30, 50, 100]"
        />
      </template>
    </VCard>
  </div>
</template>
