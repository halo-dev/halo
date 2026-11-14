<script lang="ts" setup>
import {
  PluginStatusPhaseEnum,
  consoleApiClient,
  paginate,
  type Plugin,
  type PluginV1alpha1ConsoleApiListPluginsRequest,
} from "@halo-dev/api-client";
import {
  IconAddCircle,
  IconPlug,
  IconRefreshLine,
  IconSettings,
  VButton,
  VCard,
  VDropdown,
  VDropdownItem,
  VEmpty,
  VEntityContainer,
  VFilterDropdown,
  VFilterDropdownGroup,
  VLoading,
  VPageHeader,
  VSpace,
} from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { useQuery } from "@tanstack/vue-query";
import { useFuse } from "@vueuse/integrations/useFuse";
import { useRouteQuery } from "@vueuse/router";
import type { Ref } from "vue";
import { computed, provide, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import PluginInstallationModal from "./components/PluginInstallationModal.vue";
import PluginListItem from "./components/PluginListItem.vue";
import { usePluginBatchOperations } from "./composables/use-plugin";

const { t } = useI18n();

const pluginInstallationModalVisible = ref(false);

const keyword = useRouteQuery<string>("keyword", "");

const selectedEnabledValue = useRouteQuery<string | undefined>("enabled");
const selectedAuthorName = useRouteQuery<string | undefined>("author");
const selectedAuthorWebsite = useRouteQuery<string | undefined>(
  "authorWebsite"
);
const selectedSortValue = useRouteQuery<string | undefined>("sort");

function getAuthorKey(author: Plugin["spec"]["author"]) {
  return JSON.stringify([author?.name, author?.website?.trim() || ""]);
}

const selectedAuthor = computed({
  get: () =>
    selectedAuthorName.value
      ? getAuthorKey({
          name: selectedAuthorName.value,
          website: selectedAuthorWebsite.value,
        })
      : undefined,
  set: (value: string | undefined) => {
    const [name, website] = value
      ? (JSON.parse(value) as [string, string])
      : [];
    selectedAuthorName.value = name;
    selectedAuthorWebsite.value = website || undefined;
  },
});

const { data, isLoading, isFetching, refetch } = useQuery({
  queryKey: ["plugins", selectedEnabledValue, selectedSortValue],
  queryFn: async () => {
    return await paginate<PluginV1alpha1ConsoleApiListPluginsRequest, Plugin>(
      (params) => consoleApiClient.plugin.plugin.listPlugins(params),
      {
        size: 1000,
        enabled: selectedEnabledValue.value
          ? JSON.parse(selectedEnabledValue.value)
          : undefined,
        sort: [selectedSortValue.value].filter(Boolean) as string[],
      }
    );
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

const authorOptions = computed(() => {
  const authors = new Map<
    string,
    { name: string; count: number; website?: string; homepage?: string }
  >();

  for (const plugin of data.value || []) {
    const name = plugin.spec.author?.name;
    if (!name?.trim()) continue;

    const key = getAuthorKey(plugin.spec.author);
    const author = authors.get(key) || {
      name,
      count: 0,
      website: plugin.spec.author?.website?.trim() || undefined,
    };
    author.count++;
    author.homepage ||= plugin.spec.homepage?.trim() || undefined;
    authors.set(key, author);
  }

  return [...authors]
    .map(([key, author]) => ({
      label: author.name,
      value: key,
      description: author.website || author.homepage,
      count: author.count,
    }))
    .sort(
      (a, b) =>
        b.count - a.count ||
        a.label.localeCompare(b.label) ||
        a.value.localeCompare(b.value)
    );
});

const filteredPlugins = computed(() => {
  const plugins = data.value || [];
  return selectedAuthor.value
    ? plugins.filter(
        (plugin) => getAuthorKey(plugin.spec.author) === selectedAuthor.value
      )
    : plugins;
});

const { results } = useFuse(keyword, filteredPlugins, {
  fuseOptions: {
    keys: ["metadata.name", "spec.displayName", "spec.description"],
  },
  matchAllWhenSearchEmpty: true,
});

// selection
const selectedNames = ref<string[]>([]);
provide<Ref<string[]>>("selectedNames", selectedNames);
const checkedAll = ref(false);

watch(selectedAuthor, () => {
  selectedNames.value = [];
});

watch(
  () => selectedNames.value,
  (value) => {
    checkedAll.value =
      value.length > 0 && value.length === results.value?.length;
  }
);

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;
  if (checked) {
    selectedNames.value =
      results.value?.map((plugin) => {
        return plugin.item.metadata.name;
      }) || [];
  } else {
    selectedNames.value.length = 0;
  }
};

const { handleChangeStatusInBatch, handleUninstallInBatch } =
  usePluginBatchOperations(selectedNames);
</script>
<template>
  <PluginInstallationModal
    v-if="
      pluginInstallationModalVisible &&
      utils.permission.has(['system:plugins:manage'])
    "
    @close="pluginInstallationModalVisible = false"
  />

  <VPageHeader :title="$t('core.plugin.title')">
    <template #icon>
      <IconPlug />
    </template>
    <template #actions>
      <HasPermission :permissions="['*']">
        <VButton
          size="sm"
          @click="$router.push({ name: 'PluginExtensionPointSettings' })"
        >
          <template #icon>
            <IconSettings />
          </template>
          {{ $t("core.plugin.actions.extension-point-settings") }}
        </VButton>
      </HasPermission>

      <VButton
        v-permission="['system:plugins:manage']"
        type="secondary"
        @click="pluginInstallationModalVisible = true"
      >
        <template #icon>
          <IconAddCircle />
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
              v-permission="['system:plugins:manage']"
              class="hidden items-center sm:flex"
            >
              <input
                v-model="checkedAll"
                type="checkbox"
                @change="handleCheckAllChange"
              />
            </div>
            <div class="flex w-full flex-1 items-center gap-2 sm:w-auto">
              <SearchInput
                v-if="!selectedNames.length"
                v-model="keyword"
                sync
              />
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
              <VFilterDropdownGroup
                :clear-label="$t('core.common.buttons.reset')"
              >
                <VFilterDropdown
                  v-model="selectedEnabledValue"
                  :label="$t('core.common.filters.labels.status')"
                  show-clear-option
                  :clear-label="t('core.common.filters.item_labels.all')"
                  :options="[
                    {
                      label: t('core.plugin.filters.status.items.active'),
                      value: 'true',
                    },
                    {
                      label: t('core.plugin.filters.status.items.inactive'),
                      value: 'false',
                    },
                  ]"
                />
                <VFilterDropdown
                  v-model="selectedAuthor"
                  :label="$t('core.plugin.filters.author.label')"
                  :options="authorOptions"
                  :selected-label="selectedAuthorName"
                  searchable
                  :search-placeholder="$t('core.common.placeholder.search')"
                  :empty-text="$t('core.components.global_search.no_results')"
                  show-clear-option
                  :clear-label="t('core.common.filters.item_labels.all')"
                  panel-width="22rem"
                >
                  <template #option="{ option, selected }">
                    <div class="flex min-w-0 flex-1 flex-col">
                      <span
                        class="truncate text-sm"
                        :class="
                          selected
                            ? 'font-medium text-gray-900'
                            : 'text-gray-700'
                        "
                      >
                        {{ option.label }}
                      </span>
                      <span
                        v-if="option.description"
                        class="truncate text-xs text-gray-500"
                        :title="option.description"
                      >
                        {{ option.description }}
                      </span>
                    </div>
                    <span
                      v-if="option.count"
                      class="shrink-0 text-xs text-gray-500"
                    >
                      {{ option.count }}
                    </span>
                  </template>
                </VFilterDropdown>
                <VFilterDropdown
                  v-model="selectedSortValue"
                  :label="$t('core.common.filters.labels.sort')"
                  show-clear-option
                  :clear-label="t('core.common.filters.item_labels.default')"
                  :options="[
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
              </VFilterDropdownGroup>
              <div class="flex flex-row gap-2">
                <button
                  v-tooltip="$t('core.common.buttons.refresh')"
                  type="button"
                  class="group cursor-pointer rounded p-1 hover:bg-gray-200"
                  @click="refetch()"
                >
                  <IconRefreshLine
                    :class="{ 'animate-spin text-gray-900': isFetching }"
                    class="h-4 w-4 text-gray-600 group-hover:text-gray-900"
                  />
                </button>
              </div>
            </VSpace>
          </div>
        </div>
      </template>

      <VLoading v-if="isLoading" />

      <Transition v-else-if="!results?.length" appear name="fade">
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
                  <IconAddCircle />
                </template>
                {{ $t("core.plugin.empty.actions.install") }}
              </VButton>
            </VSpace>
          </template>
        </VEmpty>
      </Transition>

      <Transition v-else appear name="fade">
        <VEntityContainer>
          <PluginListItem
            v-for="result in results"
            :key="result.item.metadata.name"
            :plugin="result.item"
            :is-selected="selectedNames.includes(result.item.metadata.name)"
          />
        </VEntityContainer>
      </Transition>

      <template #footer>
        <div class="flex h-8 items-center">
          <span class="text-sm text-gray-500">
            {{
              $t("core.components.pagination.total_label", {
                total: results?.length,
              })
            }}
          </span>
        </div>
      </template>
    </VCard>
  </div>
</template>
