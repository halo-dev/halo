<script lang="ts" setup>
import {
  IconAddCircle,
  IconArrowDown,
  IconPlug,
  VButton,
  VCard,
  VEmpty,
  VPageHeader,
  VPagination,
  VSpace,
  VTag,
} from "@halo-dev/components";
import PluginListItem from "./components/PluginListItem.vue";
import PluginInstallModal from "./components/PluginInstallModal.vue";
import { onMounted, ref, watch } from "vue";
import { apiClient } from "@halo-dev/admin-shared";
import type { Plugin } from "@halo-dev/api-client";

const plugins = ref<Plugin[]>([] as Plugin[]);
const loading = ref(false);
const pluginInstall = ref(false);
const keyword = ref("");

const handleFetchPlugins = async () => {
  try {
    loading.value = true;

    const fieldSelector: Array<string> = [];

    if (keyword.value) {
      fieldSelector.push(`name=${keyword.value}`);
    }

    const { data } =
      await apiClient.extension.plugin.listpluginHaloRunV1alpha1Plugin(
        0,
        0,
        [],
        fieldSelector
      );
    plugins.value = data.items;
  } catch (e) {
    console.error("Fail to fetch plugins", e);
  } finally {
    loading.value = false;
  }
};

watch(
  () => keyword.value,
  () => {
    handleFetchPlugins();
  }
);

onMounted(handleFetchPlugins);
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
            <div class="flex w-full flex-1 sm:w-auto">
              <FormKit
                v-model="keyword"
                placeholder="输入关键词搜索"
                type="text"
              ></FormKit>
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
                          v-close-popper
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">已启用</span>
                        </li>
                        <li
                          v-close-popper
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">未启用</span>
                        </li>
                      </ul>
                    </div>
                  </template>
                </FloatingDropdown>
                <div
                  class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                >
                  <span class="mr-0.5">类别</span>
                  <span>
                    <IconArrowDown />
                  </span>
                </div>
                <FloatingDropdown>
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">提供方</span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <div class="h-96 w-80 p-4">
                      <div class="bg-white">
                        <!--TODO: Auto Focus-->
                        <FormKit
                          placeholder="输入关键词搜索"
                          type="text"
                        ></FormKit>
                      </div>
                      <div class="mt-2">
                        <ul class="divide-y divide-gray-200" role="list">
                          <li class="cursor-pointer py-4 hover:bg-gray-50">
                            <div class="flex items-center space-x-4">
                              <div class="flex items-center">
                                <input
                                  class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                                  type="checkbox"
                                />
                              </div>
                              <div class="flex-shrink-0">
                                <img
                                  alt="halo-dev"
                                  class="h-10 w-10 rounded"
                                  src="https://halo.run/logo"
                                />
                              </div>
                              <div class="min-w-0 flex-1">
                                <p
                                  class="truncate text-sm font-medium text-gray-900"
                                >
                                  Halo
                                </p>
                                <p class="truncate text-sm text-gray-500">
                                  https://halo.run
                                </p>
                              </div>
                              <div>
                                <VTag>2 个</VTag>
                              </div>
                            </div>
                          </li>
                        </ul>
                      </div>
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
                          v-close-popper
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">较近安装</span>
                        </li>
                        <li
                          v-close-popper
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">较晚安装</span>
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
        v-if="!plugins.length && !loading"
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
        <li v-for="(plugin, index) in plugins" :key="index">
          <PluginListItem :plugin="plugin" />
        </li>
      </ul>

      <template #footer>
        <div class="bg-white sm:flex sm:items-center sm:justify-end">
          <VPagination :page="1" :size="10" :total="20" />
        </div>
      </template>
    </VCard>
  </div>
</template>
