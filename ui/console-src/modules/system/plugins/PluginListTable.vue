<script lang="ts" setup>
import PluginTableActions from "@console/modules/table-demo/PluginTableActions.vue";
import PluginTableSwitch from "@console/modules/table-demo/PluginTableSwitch.vue";
import {
  PluginStatusPhaseEnum,
  consoleApiClient,
  paginate,
  type Plugin,
  type PluginV1alpha1ConsoleApiListPluginsRequest,
} from "@halo-dev/api-client";
import {
  IconAddCircle,
  IconInformation,
  IconPlug,
  IconRefreshLine,
  IconSettings,
  VAvatar,
  VButton,
  VCard,
  VDropdown,
  VDropdownItem,
  VEmpty,
  VFilterDropdown,
  VFilterDropdownGroup,
  VPageHeader,
  VSpace,
  VStatusDot,
  VTable,
  VTag,
  createTableColumnHelper,
  type ColumnPinningState,
  type RowSelectionState,
} from "@halo-dev/components";
import { stores, utils } from "@halo-dev/ui-shared";
import { useQuery } from "@tanstack/vue-query";
import { useFuse } from "@vueuse/integrations/useFuse";
import { useRouteQuery } from "@vueuse/router";
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { pluginLabels } from "@/constants/labels";
import PluginInstallationModal from "./components/PluginInstallationModal.vue";
import { usePluginBatchOperations } from "./composables/use-plugin";

const { t } = useI18n();

const uiPluginsStore = stores.uiPlugins();

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

const plugins = computed(
  () => results.value?.map((result) => result.item) || []
);

// selection
const rowSelection = ref<RowSelectionState>({});

const columnPinning = ref<ColumnPinningState>({
  start: ["__select", "plugin"],
  end: ["actions"],
});

const selectedNames = computed<string[]>({
  get: () =>
    Object.keys(rowSelection.value).filter((name) => rowSelection.value[name]),
  set: (names) => {
    rowSelection.value = Object.fromEntries(names.map((name) => [name, true]));
  },
});

watch(selectedAuthor, () => {
  selectedNames.value = [];
});

const { handleChangeStatusInBatch, handleUninstallInBatch } =
  usePluginBatchOperations(selectedNames);

function getStatusDotState(plugin: Plugin) {
  const { phase } = plugin.status || {};
  const { enabled } = plugin.spec;

  if (enabled && phase === PluginStatusPhaseEnum.Failed) {
    return "error";
  }

  if (phase === PluginStatusPhaseEnum.Disabling) {
    return "warning";
  }

  return "default";
}

function isTransitional(plugin: Plugin) {
  const { phase } = plugin.status || {};
  return (
    phase !== PluginStatusPhaseEnum.Started &&
    phase !== PluginStatusPhaseEnum.Disabled
  );
}

function getStatusMessage(plugin: Plugin) {
  const { phase } = plugin.status || {};

  if (
    phase === PluginStatusPhaseEnum.Failed ||
    phase === PluginStatusPhaseEnum.Disabling
  ) {
    const lastCondition = plugin.status?.conditions?.[0];

    return (
      [lastCondition?.reason, lastCondition?.message]
        .filter(Boolean)
        .join(": ") || "Unknown"
    );
  }

  return t("core.common.status.starting_up");
}

function needsReloadWindow(plugin: Plugin) {
  const currentJsModuleInfo = uiPluginsStore.get(plugin.metadata.name);

  if (!currentJsModuleInfo) {
    return false;
  }

  return (
    plugin.status?.phase === PluginStatusPhaseEnum.Started &&
    plugin.spec.version !== currentJsModuleInfo.version
  );
}

function handleReloadWindow() {
  window.location.reload();
}

function getPluginRowKey(plugin: Plugin) {
  return plugin.metadata.name;
}

const column = createTableColumnHelper<Plugin>();

const columns = column.columns([
  column.display({
    id: "plugin",
    header: "插件",
    meta: { minWidth: 340 },
  }),
  column.display({
    id: "author",
    header: "作者",
    meta: { width: 150 },
  }),
  column.accessor((plugin: Plugin) => plugin.spec.version, {
    id: "version",
    header: "版本",
    enableSorting: true,
    sortFn: "alphanumeric",
    meta: { width: 110 },
  }),
  column.display({
    id: "status",
    header: "状态",
    meta: { width: 130 },
  }),
  column.display({
    id: "enabled",
    header: "启用",
    meta: { width: 90, align: "center" },
  }),
  column.display({
    id: "actions",
    header: "操作",
    meta: { width: 90, align: "right" },
  }),
]);
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
            <div class="flex w-full flex-1 items-center gap-2 sm:w-auto">
              <SearchInput
                v-if="!selectedNames.length"
                v-model="keyword"
                sync
              />
              <VSpace v-else>
                <span class="text-sm text-gray-500">
                  已选择 {{ selectedNames.length }} 项
                </span>
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

      <VTable
        v-model:column-pinning="columnPinning"
        v-model:row-selection="rowSelection"
        :columns="columns"
        :data="plugins"
        :loading="isLoading"
        :row-key="getPluginRowKey"
        :selectable="utils.permission.has(['system:plugins:manage'])"
        header-menu
        pinnable
        resizable
        select-all-label="选择全部插件"
        select-row-label="选择插件"
      >
        <template #empty>
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
        </template>

        <template #cell-plugin="{ original }">
          <div class="flex items-start gap-3">
            <VAvatar
              :alt="original.spec.displayName"
              :src="original.status?.logo"
              size="md"
            />
            <div class="min-w-0">
              <div class="flex items-center gap-2">
                <RouterLink
                  :to="{
                    name: 'PluginDetail',
                    params: { name: original.metadata.name },
                  }"
                  class="truncate font-medium text-gray-900 hover:text-primary"
                >
                  {{ original.spec.displayName }}
                </RouterLink>
                <VTag
                  v-if="
                    original.metadata.labels?.[pluginLabels.SYSTEM_RESERVED] ===
                    'true'
                  "
                >
                  {{ $t("core.plugin.list.fields.system_reserved") }}
                </VTag>
              </div>
              <p
                class="mt-1 line-clamp-2 whitespace-normal text-xs text-gray-500"
              >
                {{ original.spec.description }}
              </p>
            </div>
          </div>
        </template>

        <template #cell-author="{ original }">
          <a
            v-if="original.spec.author"
            :href="original.spec.author.website"
            class="text-sm text-gray-500 hover:text-gray-900"
            target="_blank"
          >
            @{{ original.spec.author.name }}
          </a>
          <span v-else class="text-sm text-gray-400">-</span>
        </template>

        <template #cell-version="{ original }">
          <span class="text-sm text-gray-500">
            {{ original.spec.version }}
          </span>
        </template>

        <template #cell-status="{ original }">
          <VStatusDot
            v-if="original.metadata.deletionTimestamp"
            :text="$t('core.common.status.deleting')"
            state="warning"
            animate
          />
          <VStatusDot
            v-else-if="original.spec.enabled && isTransitional(original)"
            v-tooltip="getStatusMessage(original)"
            :state="getStatusDotState(original)"
            :text="getStatusMessage(original)"
            animate
          />
          <span v-else class="text-sm text-gray-600">
            {{
              original.spec.enabled
                ? $t("core.plugin.filters.status.items.active")
                : $t("core.plugin.filters.status.items.inactive")
            }}
          </span>
        </template>

        <template #cell-enabled="{ original }">
          <PluginTableSwitch :plugin="original" />
        </template>

        <template #cell-actions="{ original }">
          <div class="flex items-center justify-end gap-2">
            <VButton
              v-if="needsReloadWindow(original)"
              size="xs"
              @click="handleReloadWindow"
            >
              <template #icon>
                <IconInformation />
              </template>
              {{ $t("core.plugin.operations.reload_window.button") }}
            </VButton>
            <PluginTableActions :plugin="original" />
          </div>
        </template>
      </VTable>

      <template #footer>
        <div class="flex h-8 items-center">
          <span class="text-sm text-gray-500">
            {{
              $t("core.components.pagination.total_label", {
                total: plugins.length,
              })
            }}
          </span>
        </div>
      </template>
    </VCard>
  </div>
</template>
