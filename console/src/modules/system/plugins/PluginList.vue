<script lang="ts" setup>
import {
  IconAddCircle,
  IconArrowDown,
  IconPlug,
  IconRefreshLine,
  VButton,
  VCard,
  VEmpty,
  VPageHeader,
  VPagination,
  VSpace,
  VLoading,
} from "@halo-dev/components";
import PluginListItem from "./components/PluginListItem.vue";
import PluginUploadModal from "./components/PluginUploadModal.vue";
import { computed, ref } from "vue";
import { apiClient } from "@/utils/api-client";
import { usePermission } from "@/utils/permission";
import FilterTag from "@/components/filter/FilterTag.vue";
import FilteCleanButton from "@/components/filter/FilterCleanButton.vue";
import { getNode } from "@formkit/core";
import { useQuery } from "@tanstack/vue-query";
import type { Plugin } from "@halo-dev/api-client";

interface EnabledItem {
  label: string;
  value?: boolean;
}

interface SortItem {
  label: string;
  value: string;
}

const { currentUserHasPermission } = usePermission();

const pluginInstall = ref(false);

const keyword = ref("");
const page = ref(1);
const size = ref(20);
const total = ref(0);

// Filters
const EnabledItems: EnabledItem[] = [
  {
    label: "全部",
    value: undefined,
  },
  {
    label: "已启用",
    value: true,
  },
  {
    label: "未启用",
    value: false,
  },
];

const SortItems: SortItem[] = [
  {
    label: "较近安装",
    value: "creationTimestamp,desc",
  },
  {
    label: "较早安装",
    value: "creationTimestamp,asc",
  },
];

const selectedEnabledItem = ref<EnabledItem>();
const selectedSortItem = ref<SortItem>();

function handleEnabledItemChange(enabledItem: EnabledItem) {
  selectedEnabledItem.value = enabledItem;
  page.value = 1;
}

function handleSortItemChange(sortItem?: SortItem) {
  selectedSortItem.value = sortItem;
  page.value = 1;
}

function handleKeywordChange() {
  const keywordNode = getNode("keywordInput");
  if (keywordNode) {
    keyword.value = keywordNode._value as string;
  }
  page.value = 1;
}

function handleClearKeyword() {
  keyword.value = "";
  page.value = 1;
}

const hasFilters = computed(() => {
  return (
    selectedEnabledItem.value?.value !== undefined ||
    selectedSortItem.value?.value ||
    keyword.value
  );
});

function handleClearFilters() {
  selectedEnabledItem.value = undefined;
  selectedSortItem.value = undefined;
  keyword.value = "";
  page.value = 1;
}

const { data, isLoading, isFetching, refetch } = useQuery<Plugin[]>({
  queryKey: [
    "plugins",
    page,
    size,
    keyword,
    selectedEnabledItem,
    selectedSortItem,
  ],
  queryFn: async () => {
    const { data } = await apiClient.plugin.listPlugins({
      page: page.value,
      size: size.value,
      keyword: keyword.value,
      enabled: selectedEnabledItem.value?.value,
      sort: [selectedSortItem.value?.value].filter(Boolean) as string[],
    });

    total.value = data.total;

    return data.items;
  },
  refetchOnWindowFocus: false,
  keepPreviousData: true,
  refetchInterval: (data) => {
    const deletingPlugins = data?.filter(
      (plugin) => !!plugin.metadata.deletionTimestamp
    );

    return deletingPlugins?.length ? 3000 : false;
  },
});
</script>
<template>
  <PluginUploadModal
    v-if="currentUserHasPermission(['system:plugins:manage'])"
    v-model:visible="pluginInstall"
    @close="refetch()"
  />

  <VPageHeader title="插件">
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
        安装
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
              <FormKit
                id="keywordInput"
                outer-class="!p-0"
                placeholder="输入关键词搜索"
                type="text"
                name="keyword"
                :model-value="keyword"
                @keyup.enter="handleKeywordChange"
              ></FormKit>

              <FilterTag v-if="keyword" @close="handleClearKeyword()">
                关键词：{{ keyword }}
              </FilterTag>

              <FilterTag
                v-if="selectedEnabledItem?.value !== undefined"
                @close="handleEnabledItemChange(EnabledItems[0])"
              >
                启用状态：{{ selectedEnabledItem.label }}
              </FilterTag>

              <FilterTag
                v-if="selectedSortItem"
                @close="handleSortItemChange()"
              >
                排序：{{ selectedSortItem.label }}
              </FilterTag>

              <FilteCleanButton v-if="hasFilters" @click="handleClearFilters" />
            </div>
            <div class="mt-4 flex sm:mt-0">
              <VSpace spacing="lg">
                <FloatingDropdown>
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">启用状态</span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <div class="w-52 p-4">
                      <ul class="space-y-1">
                        <li
                          v-for="(enabledItem, index) in EnabledItems"
                          :key="index"
                          v-close-popper
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                          :class="{
                            'bg-gray-100':
                              selectedEnabledItem?.value === enabledItem.value,
                          }"
                          @click="handleEnabledItemChange(enabledItem)"
                        >
                          <span class="truncate">{{ enabledItem.label }}</span>
                        </li>
                      </ul>
                    </div>
                  </template>
                </FloatingDropdown>
                <FloatingDropdown>
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">排序</span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <div class="w-72 p-4">
                      <ul class="space-y-1">
                        <li
                          v-for="(sortItem, index) in SortItems"
                          :key="index"
                          v-close-popper
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                          @click="handleSortItemChange(sortItem)"
                        >
                          <span class="truncate">{{ sortItem.label }}</span>
                        </li>
                      </ul>
                    </div>
                  </template>
                </FloatingDropdown>
                <div class="flex flex-row gap-2">
                  <div
                    class="group cursor-pointer rounded p-1 hover:bg-gray-200"
                    @click="refetch()"
                  >
                    <IconRefreshLine
                      v-tooltip="`刷新`"
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
          message="当前没有已安装的插件，你可以尝试刷新或者安装新插件"
          title="当前没有已安装的插件"
        >
          <template #actions>
            <VSpace>
              <VButton :loading="isFetching" @click="refetch()">刷新</VButton>
              <VButton
                v-permission="['system:plugins:manage']"
                type="secondary"
                @click="pluginInstall = true"
              >
                <template #icon>
                  <IconAddCircle class="h-full w-full" />
                </template>
                安装插件
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
          <li v-for="(plugin, index) in data" :key="index">
            <PluginListItem :plugin="plugin" @reload="refetch()" />
          </li>
        </ul>
      </Transition>

      <template #footer>
        <div class="bg-white sm:flex sm:items-center sm:justify-end">
          <VPagination
            v-model:page="page"
            v-model:size="size"
            :total="total"
            :size-options="[10, 20, 30, 50, 100]"
          />
        </div>
      </template>
    </VCard>
  </div>
</template>
