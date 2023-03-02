<script lang="ts" setup>
import {
  IconAddCircle,
  IconDeleteBin,
  IconStopCircle,
  VButton,
  VModal,
  VSpace,
  VTabItem,
  VTabs,
} from "@halo-dev/components";
import { onMounted, ref } from "vue";
import { apiClient } from "@/utils/api-client";
import type { PersonalAccessToken } from "@halo-dev/api-client";

const createVisible = ref(false);
const createActiveId = ref("general");

const personalAccessTokens = ref<PersonalAccessToken[]>([]);

const handleFetchPersonalAccessTokens = async () => {
  try {
    const { data } =
      await apiClient.extension.personalAccessToken.listv1alpha1PersonalAccessToken();
    personalAccessTokens.value = data.items;
  } catch (e) {
    console.error(e);
  }
};

onMounted(() => {
  handleFetchPersonalAccessTokens();
});
</script>
<template>
  <VModal v-model:visible="createVisible" :width="720" title="创建个人令牌">
    <VTabs v-model:active-id="createActiveId" type="outline">
      <VTabItem id="general" label="基础信息">
        <FormKit id="role-form" name="role-form" :actions="false" type="form">
          <FormKit label="名称" type="text" validation="required"></FormKit>
          <FormKit label="失效日期" type="text" validation="required"></FormKit>
        </FormKit>
      </VTabItem>
      <VTabItem id="permissions" label="权限">
        <div>
          <dl class="divide-y divide-gray-100">
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">
                Posts Management
              </dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <ul class="space-y-2">
                  <li>
                    <div
                      class="inline-flex w-72 cursor-pointer flex-row items-center gap-4 rounded border p-5 hover:border-primary"
                    >
                      <input
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                      />
                      <div class="inline-flex flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          Posts Management
                        </span>
                        <span class="text-xs text-gray-400">
                          依赖于 Posts View
                        </span>
                      </div>
                    </div>
                  </li>
                  <li>
                    <div
                      class="inline-flex w-72 cursor-pointer items-center gap-4 rounded border p-5 hover:border-primary"
                    >
                      <input
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                      />
                      <div class="inline-flex flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          Posts View
                        </span>
                      </div>
                    </div>
                  </li>
                </ul>
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">
                Categories Management
              </dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <ul class="space-y-2">
                  <li>
                    <div
                      class="inline-flex w-72 cursor-pointer flex-row items-center gap-4 rounded border p-5 hover:border-primary"
                    >
                      <input
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                      />
                      <div class="inline-flex flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          Categories Management
                        </span>
                        <span class="text-xs text-gray-400">
                          依赖于 Categories View
                        </span>
                      </div>
                    </div>
                  </li>
                  <li>
                    <div
                      class="inline-flex w-72 cursor-pointer items-center gap-4 rounded border p-5 hover:border-primary"
                    >
                      <input
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                      />
                      <div class="inline-flex flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          Categories View
                        </span>
                      </div>
                    </div>
                  </li>
                </ul>
              </dd>
            </div>
            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">Tags Management</dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <ul class="space-y-2">
                  <li>
                    <div
                      class="inline-flex w-72 cursor-pointer flex-row items-center gap-4 rounded border p-5 hover:border-primary"
                    >
                      <input
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                      />
                      <div class="inline-flex flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          Tags Management
                        </span>
                        <span class="text-xs text-gray-400">
                          依赖于 Tags View
                        </span>
                      </div>
                    </div>
                  </li>
                  <li>
                    <div
                      class="inline-flex w-72 cursor-pointer items-center gap-4 rounded border p-5 hover:border-primary"
                    >
                      <input
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                      />
                      <div class="inline-flex flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          Tags View
                        </span>
                      </div>
                    </div>
                  </li>
                </ul>
              </dd>
            </div>

            <div
              class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
            >
              <dt class="text-sm font-medium text-gray-900">
                Plugins Management
              </dt>
              <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                <ul class="space-y-2">
                  <li>
                    <div
                      class="inline-flex w-72 cursor-pointer flex-row items-center gap-4 rounded border p-5 hover:border-primary"
                    >
                      <input
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                      />
                      <div class="inline-flex flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          Plugins Management
                        </span>
                        <span class="text-xs text-gray-400">
                          依赖于 Plugins View
                        </span>
                      </div>
                    </div>
                  </li>
                  <li>
                    <div
                      class="inline-flex w-72 cursor-pointer items-center gap-4 rounded border p-5 hover:border-primary"
                    >
                      <input
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                        type="checkbox"
                      />
                      <div class="inline-flex flex-col gap-y-3">
                        <span class="font-medium text-gray-900">
                          Plugins View
                        </span>
                      </div>
                    </div>
                  </li>
                </ul>
              </dd>
            </div>
          </dl>
        </div>
      </VTabItem>
    </VTabs>
    <template #footer>
      <VButton type="secondary">创建</VButton>
    </template>
  </VModal>

  <div class="mt-5 flex justify-end">
    <VSpace>
      <VButton size="sm" type="danger">
        <template #icon>
          <IconStopCircle class="h-full w-full" />
        </template>
        禁用所有
      </VButton>
      <VButton type="secondary" @click="createVisible = true">
        <template #icon>
          <IconAddCircle class="h-full w-full" />
        </template>
        创建
      </VButton>
    </VSpace>
  </div>
  <ul
    class="mt-5 box-border h-full w-full divide-y divide-gray-100"
    role="list"
  >
    <li v-for="(token, index) in personalAccessTokens" :key="index">
      <div
        class="relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
      >
        <div class="relative flex flex-row items-center">
          <div class="flex-1">
            <div class="flex flex-row items-center">
              <span class="mr-2 truncate text-sm font-medium text-gray-900">
                {{ token.spec?.displayName }}
              </span>
            </div>
            <div class="mt-2 flex">
              <span class="text-xs text-gray-500">
                {{ token.spec?.tokenDigest }}
              </span>
            </div>
          </div>
          <div class="flex">
            <div
              class="inline-flex flex-col items-end gap-4 sm:flex-row sm:items-center sm:gap-6"
            >
              <div class="flex flex-col gap-1">
                <time class="text-xs text-gray-500" datetime="2020-01-07">
                  创建日期：{{ token.metadata?.creationTimestamp }}
                </time>
                <time class="text-xs text-gray-500" datetime="2020-01-07">
                  失效日期：{{ token.spec?.expiresAt }}
                </time>
              </div>
              <span class="cursor-pointer hover:text-red-600">
                <IconStopCircle />
              </span>
              <span class="cursor-pointer hover:text-red-600">
                <IconDeleteBin />
              </span>
            </div>
          </div>
        </div>
      </div>
    </li>
  </ul>
</template>
