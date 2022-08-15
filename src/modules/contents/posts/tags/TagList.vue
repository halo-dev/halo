<script lang="ts" setup>
import { ref } from "vue";
import {
  IconAddCircle,
  IconBookRead,
  IconGrid,
  IconList,
  IconSettings,
  VButton,
  VCard,
  VPageHeader,
  VSpace,
  VTag,
} from "@halo-dev/components";
import TagEditingModal from "./components/TagEditingModal.vue";

const editingModal = ref(false);
const viewTypes = [
  {
    name: "list",
    icon: IconList,
  },
  {
    name: "grid",
    icon: IconGrid,
  },
];

const viewType = ref("list");
</script>
<template>
  <TagEditingModal v-model:visible="editingModal" />
  <VPageHeader title="文章标签">
    <template #icon>
      <IconBookRead class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <VButton type="secondary" @click="editingModal = true">
          <template #icon>
            <IconAddCircle class="h-full w-full" />
          </template>
          新建
        </VButton>
      </VSpace>
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
              <span class="text-base font-medium"> {{ 10 }} 个标签 </span>
            </div>
            <div class="flex flex-row gap-2">
              <div
                v-for="(item, index) in viewTypes"
                :key="index"
                :class="{
                  'bg-gray-200 font-bold text-black': viewType === item.name,
                }"
                class="cursor-pointer rounded p-1 hover:bg-gray-200"
                @click="viewType = item.name"
              >
                <component :is="item.icon" />
              </div>
            </div>
          </div>
        </div>
      </template>
      <ul
        v-if="viewType === 'list'"
        class="box-border h-full w-full divide-y divide-gray-100"
        role="list"
      >
        <li v-for="i in 10" :key="i">
          <div
            class="relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
          >
            <div class="relative flex flex-row items-center">
              <div class="flex-1">
                <div class="flex flex-col sm:flex-row">
                  <VTag>主题</VTag>
                </div>
                <div class="mt-1 flex">
                  <span class="text-xs text-gray-500">
                    https://halo.run/tags/themes
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
                            <VButton
                              v-close-popper
                              block
                              type="secondary"
                              @click="editingModal = true"
                            >
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

      <div v-else class="flex flex-wrap gap-3 p-4" role="list">
        <VTag v-for="i in 100" :key="i">主题(10)</VTag>
      </div>
    </VCard>
  </div>
</template>
