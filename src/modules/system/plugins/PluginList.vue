<script lang="ts" setup>
import {
  IconAddCircle,
  IconArrowDown,
  IconCloseCircle,
  IconPlug,
  VButton,
  VCard,
  VEmpty,
  VPageHeader,
  VPagination,
  VSpace,
} from "@halo-dev/components";
import PluginListItem from "./components/PluginListItem.vue";
import PluginInstallModal from "./components/PluginInstallModal.vue";
import { onMounted, ref } from "vue";
import { apiClient } from "@/utils/api-client";
import type { PluginList } from "@halo-dev/api-client";

const plugins = ref<PluginList>({
  page: 1,
  size: 20,
  total: 0,
  items: [],
  first: true,
  last: false,
  hasNext: false,
  hasPrevious: false,
});
const loading = ref(false);
const pluginInstall = ref(false);
const keyword = ref("");

const handleFetchPlugins = async () => {
  try {
    loading.value = true;

    const { data } = await apiClient.plugin.listPlugins({
      page: plugins.value.page,
      size: plugins.value.size,
      keyword: keyword.value,
      enabled: selectedEnabledItem.value?.value,
      sort: [selectedSortItem.value?.value].filter(
        (item) => !!item
      ) as string[],
    });

    plugins.value = data;
  } catch (e) {
    console.error("Failed to fetch plugins", e);
  } finally {
    loading.value = false;
  }
};

const handlePaginationChange = ({
  page,
  size,
}: {
  page: number;
  size: number;
}) => {
  plugins.value.page = page;
  plugins.value.size = size;
  handleFetchPlugins();
};

onMounted(handleFetchPlugins);

// Filters
interface EnabledItem {
  label: string;
  value?: boolean;
}

interface SortItem {
  label: string;
  value: string;
}

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
  handleFetchPlugins();
}

function handleSortItemChange(sortItem?: SortItem) {
  selectedSortItem.value = sortItem;
  handleFetchPlugins();
}
</script>
<template>
  <PluginInstallModal
    v-model:visible="pluginInstall"
    v-permission="['system:plugins:manage']"
    @close="handleFetchPlugins"
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
                v-model="keyword"
                placeholder="输入关键词搜索"
                type="text"
                @keyup.enter="
                  handlePaginationChange({ page: 1, size: plugins.size })
                "
              ></FormKit>
              <div
                v-if="selectedEnabledItem?.value !== undefined"
                class="group flex cursor-pointer items-center justify-center gap-1 rounded-full bg-gray-200 px-2 py-1 hover:bg-gray-300"
              >
                <span class="text-xs text-gray-600 group-hover:text-gray-900">
                  启用状态：{{ selectedEnabledItem.label }}
                </span>
                <IconCloseCircle
                  class="h-4 w-4 text-gray-600"
                  @click="handleEnabledItemChange(EnabledItems[0])"
                />
              </div>
              <div
                v-if="selectedSortItem"
                class="group flex cursor-pointer items-center justify-center gap-1 rounded-full bg-gray-200 px-2 py-1 hover:bg-gray-300"
              >
                <span class="text-xs text-gray-600 group-hover:text-gray-900">
                  排序：{{ selectedSortItem.label }}
                </span>
                <IconCloseCircle
                  class="h-4 w-4 text-gray-600"
                  @click="handleSortItemChange()"
                />
              </div>
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
              </VSpace>
            </div>
          </div>
        </div>
      </template>

      <VEmpty
        v-if="!plugins.total && !loading"
        message="当前没有已安装的插件，你可以尝试刷新或者安装新插件"
        title="当前没有已安装的插件"
      >
        <template #actions>
          <VSpace>
            <VButton @click="handleFetchPlugins">刷新</VButton>
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

      <ul
        v-else
        class="box-border h-full w-full divide-y divide-gray-100"
        role="list"
      >
        <li v-for="(plugin, index) in plugins.items" :key="index">
          <PluginListItem :plugin="plugin" />
        </li>
      </ul>

      <template #footer>
        <div class="bg-white sm:flex sm:items-center sm:justify-end">
          <VPagination
            :page="plugins.page"
            :size="plugins.size"
            :total="plugins.total"
            :size-options="[20, 30, 50, 100]"
            @change="handlePaginationChange"
          />
        </div>
      </template>
    </VCard>
  </div>
</template>
