<script lang="ts" setup>
import {
  IconBookRead,
  IconList,
  IconSettings,
  VButton,
  VCard,
  VPageHeader,
  VSpace,
} from "@halo-dev/components";
import CategoryEditingModal from "./components/CategoryEditingModal.vue";

import { ref } from "vue";

const editingModal = ref(false);
</script>
<template>
  <CategoryEditingModal v-model:visible="editingModal" />
  <VPageHeader title="文章分类">
    <template #icon>
      <IconBookRead class="mr-2 self-center" />
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
              <span class="text-base font-medium"> {{ 10 }} 个分类 </span>
            </div>
            <div class="mt-4 flex sm:mt-0">
              <VSpace>
                <VButton size="xs" type="default" @click="editingModal = true">
                  新增
                </VButton>
              </VSpace>
            </div>
          </div>
        </div>
      </template>
      <ul class="box-border h-full w-full divide-y divide-gray-100" role="list">
        <li v-for="i in 10" :key="i">
          <div
            class="group relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
          >
            <div
              class="drag-element absolute inset-y-0 left-0 flex hidden w-3.5 cursor-move items-center bg-gray-100 transition-all hover:bg-gray-200 group-hover:flex"
            >
              <IconList class="h-3.5 w-3.5" />
            </div>
            <div class="relative flex flex-row items-center">
              <div class="flex-1">
                <div class="flex flex-col sm:flex-row">
                  <span
                    class="mr-0 truncate text-sm font-medium text-gray-900 sm:mr-2"
                  >
                    主题
                  </span>
                  <VSpace class="mt-1 sm:mt-0"></VSpace>
                </div>
                <div class="mt-1 flex">
                  <span class="text-xs text-gray-500">
                    https://halo.run/categories/themes
                  </span>
                </div>
              </div>
              <div class="flex">
                <div
                  class="inline-flex flex-col flex-col-reverse items-end gap-4 sm:flex-row sm:items-center sm:gap-6"
                >
                  <div
                    class="cursor-pointer text-sm text-gray-500 hover:text-gray-900"
                  >
                    20 篇文章
                  </div>
                  <time class="text-sm text-gray-500" datetime="2020-01-07">
                    2020-01-07
                  </time>
                  <span class="cursor-pointer">
                    <FloatingDropdown>
                      <IconSettings
                        class="cursor-pointer transition-all hover:text-blue-600"
                      />
                      <template #popper>
                        <div class="w-48 p-2">
                          <VSpace class="w-full" direction="column">
                            <VButton v-close-popper block type="secondary">
                              修改
                            </VButton>
                            <VButton v-close-popper block type="danger">
                              删除
                            </VButton>
                          </VSpace>
                        </div>
                      </template>
                    </FloatingDropdown>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </li>
      </ul>
    </VCard>
  </div>
</template>
