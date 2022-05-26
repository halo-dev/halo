<script lang="ts" setup>
import { VPageHeader } from "@/components/base/header";
import { VButton } from "@/components/base/button";
import { VModal } from "@/components/base/modal";
import { VCard } from "@/components/base/card";
import { VSpace } from "@/components/base/space";
import { VInput } from "@/components/base/input";
import { ref } from "vue";
import {
  IconArrowDown,
  IconArrowLeft,
  IconArrowRight,
  IconCheckboxFill,
  IconGrid,
  IconList,
  IconPalette,
  IconSettings,
} from "@/core/icons";

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

const viewType = ref("grid");

const strategyVisible = ref(false);
const attachmentSelectVisible = ref(false);
const uploadVisible = ref(false);
const detailVisible = ref(false);
const checkAll = ref(false);

const strategies = ref([
  {
    id: "1",
    name: "本地存储",
    description: "~/.halo/uploads",
  },
  {
    id: "2",
    name: "阿里云 OSS",
    description: "bucket/blog-attachments",
  },
  {
    id: "3",
    name: "阿里云 OSS",
    description: "bucket/blog-photos",
  },
]);

const selected = ref(strategies.value[0].id);

const attachments = Array.from(new Array(50), (_, index) => index).map(
  (index) => {
    return {
      id: index,
      name: `attachment-${index}`,
      url: `https://picsum.photos/1000/700?random=${index}`,
      size: "1.2MB",
      type: "image/png",
      strategy: "本地存储",
    };
  }
);
</script>
<template>
  <VPageHeader title="附件库">
    <template #icon>
      <IconPalette class="self-center mr-2" />
    </template>
    <template #actions>
      <VSpace>
        <VButton type="default" @click="attachmentSelectVisible = true"
          >选择
        </VButton>
        <VButton type="secondary" @click="uploadVisible = true">上传</VButton>
      </VSpace>
    </template>
  </VPageHeader>

  <VModal v-model:visible="uploadVisible" :width="800" title="上传附件">
    <div class="w-full sm:w-96">
      <VCard :bodyClass="['!p-0']" title="存储策略">
        <fieldset>
          <div class="bg-white -space-y-px divide-y divide-gray-100">
            <label
              v-for="(strategy, index) in strategies"
              :key="index"
              :class="{
                'bg-gray-50': selected === strategy.id,
              }"
              class="relative p-4 flex cursor-pointer focus:outline-none"
            >
              <input
                v-model="selected"
                :value="strategy.id"
                class="h-4 w-4 mt-0.5 cursor-pointer shrink-0 text-indigo-600 border-gray-300 focus:ring-indigo-500"
                name="privacy"
                type="radio"
              />
              <span class="ml-3 flex flex-col flex-1">
                <span
                  :class="{ 'font-bold': selected === strategy.id }"
                  class="block text-sm font-medium"
                >
                  {{ strategy.name }}
                </span>
                <span class="block text-sm text-gray-400">
                  {{ strategy.description }}
                </span>
              </span>
              <div class="self-center">
                <IconSettings
                  class="hover:text-blue-600 cursor-pointer transition-all"
                  @click.stop="strategyVisible = true"
                />
              </div>
            </label>
          </div>
        </fieldset>

        <template #footer>
          <VButton block type="secondary" @click="strategyVisible = true">
            添加策略
          </VButton>
        </template>
      </VCard>
    </div>
  </VModal>

  <VModal v-model:visible="strategyVisible" title="添加存储策略"></VModal>

  <VModal v-model:visible="detailVisible" :width="1000" title="attachment-0">
    <template #actions>
      <div class="modal-header-action">
        <IconArrowLeft />
      </div>
      <div class="modal-header-action">
        <IconArrowRight />
      </div>
    </template>
    <div class="bg-white overflow-hidden">
      <div>
        <dl>
          <div
            class="bg-gray-50 px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">原始内容</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
              <img
                class="w-full sm:w-1/2 rounded"
                src="https://picsum.photos/1000/700?random=1"
              />
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">存储策略</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
              阿里云/bucket/blog-attachments
            </dd>
          </div>
          <div
            class="bg-gray-50 px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">文件名称</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
              attachment-0
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">文件类型</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
              image/jpeg
            </dd>
          </div>
          <div
            class="bg-gray-50 px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">文件大小</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
              1.2 MB
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">上传者</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
              Ryan Wang
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">上传时间</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
              2020-01-01 12:00:00
            </dd>
          </div>
          <div
            class="bg-gray-50 px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">原始链接</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
              https://picsum.photos/1000/700?random=1
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">引用位置</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
              https://picsum.photos/1000/700?random=1
            </dd>
          </div>
        </dl>
      </div>
    </div>
    <template #footer>
      <VButton type="default" @click="detailVisible = false">关闭</VButton>
    </template>
  </VModal>

  <VModal
    v-model:visible="attachmentSelectVisible"
    :width="1240"
    title="选择附件"
  >
    <div class="w-full">
      <ul
        class="grid grid-cols-2 gap-x-2 gap-y-3 sm:grid-cols-3 md:grid-cols-2 xl:grid-cols-8 2xl:grid-cols-8"
        role="list"
      >
        <li
          v-for="(attachment, index) in attachments"
          :key="index"
          class="relative"
        >
          <VCard :body-class="['!p-0']">
            <div
              class="group block w-full aspect-w-10 aspect-h-7 bg-gray-100 overflow-hidden cursor-pointer"
            >
              <img
                :src="attachment.url"
                alt=""
                class="object-cover pointer-events-none group-hover:opacity-75"
              />
            </div>
            <p
              class="block text-sm font-medium text-gray-700 truncate pointer-events-none px-2 py-1"
            >
              {{ attachment.name }}
            </p>
          </VCard>
        </li>
      </ul>
    </div>
    <template #footer>
      <VButton type="secondary">确定</VButton>
    </template>
  </VModal>

  <div class="m-0 md:m-4">
    <div class="flex-col flex sm:flex-row gap-2">
      <div class="w-full">
        <VCard :body-class="[viewType === 'list' ? '!p-0' : '']">
          <template #header>
            <div class="px-4 py-3 block w-full bg-gray-50">
              <div
                class="flex flex-col sm:flex-row items-start sm:items-center relative"
              >
                <div class="hidden sm:flex items-center mr-4">
                  <input
                    v-model="checkAll"
                    class="h-4 w-4 text-indigo-600 border-gray-300 rounded"
                    type="checkbox"
                  />
                </div>
                <div class="w-full sm:w-auto flex flex-1">
                  <VInput
                    v-if="!checkAll"
                    class="w-full sm:w-72"
                    placeholder="输入关键词搜索"
                  />
                  <VSpace v-else>
                    <VButton type="default">设置</VButton>
                    <VButton type="danger">删除</VButton>
                  </VSpace>
                </div>
                <div class="mt-4 sm:mt-0 flex">
                  <VSpace spacing="lg">
                    <div
                      class="text-gray-700 hover:text-black cursor-pointer flex items-center text-sm"
                    >
                      <span class="mr-0.5">上传者</span>
                      <span>
                        <IconArrowDown />
                      </span>
                    </div>
                    <div
                      class="text-gray-700 hover:text-black cursor-pointer flex items-center text-sm"
                    >
                      <span class="mr-0.5">引用位置</span>
                      <span>
                        <IconArrowDown />
                      </span>
                    </div>
                    <div
                      class="text-gray-700 hover:text-black cursor-pointer flex items-center text-sm"
                    >
                      <span class="mr-0.5">标签</span>
                      <span>
                        <IconArrowDown />
                      </span>
                    </div>
                    <div
                      class="text-gray-700 hover:text-black cursor-pointer flex items-center text-sm"
                    >
                      <span class="mr-0.5">排序</span>
                      <span>
                        <IconArrowDown />
                      </span>
                    </div>
                    <div class="flex flex-row gap-2">
                      <div
                        v-for="(item, index) in viewTypes"
                        :key="index"
                        :class="{
                          'bg-gray-200 text-black font-bold':
                            viewType === item.name,
                        }"
                        class="p-1 rounded cursor-pointer hover:bg-gray-200"
                        @click="viewType = item.name"
                      >
                        <component :is="item.icon" />
                      </div>
                    </div>
                  </VSpace>
                </div>
              </div>
            </div>
          </template>

          <div
            v-if="viewType === 'grid'"
            class="grid grid-cols-3 gap-x-2 gap-y-3 sm:grid-cols-3 md:grid-cols-6 xl:grid-cols-8 2xl:grid-cols-12 mt-2"
            role="list"
          >
            <VCard
              v-for="(attachment, index) in attachments"
              :key="index"
              :body-class="['!p-0']"
              class="hover:shadow"
              @click="detailVisible = true"
            >
              <div class="relative bg-white">
                <div
                  class="group block w-full h-full aspect-w-10 aspect-h-8 bg-gray-100 overflow-hidden cursor-pointer"
                >
                  <img
                    :src="attachment.url"
                    alt=""
                    class="object-cover pointer-events-none group-hover:opacity-75"
                  />
                </div>
                <p
                  class="block text-xs font-medium text-gray-700 truncate text-center pointer-events-none px-2 py-1"
                >
                  {{ attachment.name }}
                </p>

                <IconCheckboxFill
                  v-if="checkAll"
                  class="absolute top-0.5 right-0.5"
                />
              </div>
            </VCard>
          </div>

          <ul
            v-if="viewType === 'list'"
            class="divide-y divide-gray-100 box-border w-full h-full"
            role="list"
          >
            <li v-for="(attachment, index) in attachments" :key="index">
              <div
                :class="{
                  'bg-gray-100': checkAll,
                }"
                class="px-4 py-3 block hover:bg-gray-50 cursor-pointer transition-all relative"
              >
                <div
                  v-show="checkAll"
                  class="absolute inset-y-0 left-0 w-0.5 bg-themeable-primary"
                ></div>
                <div class="flex flex-row items-center relative">
                  <div class="hidden mr-4 sm:flex items-center">
                    <input
                      v-model="checkAll"
                      class="h-4 w-4 text-indigo-600 border-gray-300 rounded"
                      type="checkbox"
                    />
                  </div>
                  <div class="flex-1">
                    <div class="flex flex-col sm:flex-row">
                      <span
                        class="mr-0 sm:mr-2 text-sm font-medium truncate text-gray-900"
                      >
                        {{ attachment.name }}
                      </span>
                    </div>
                    <div class="flex mt-1">
                      <VSpace>
                        <span class="text-xs text-gray-500">image/png</span>
                        <span class="text-xs text-gray-500">1.2 MB</span>
                      </VSpace>
                    </div>
                  </div>
                  <div class="flex">
                    <div
                      class="inline-flex flex-col items-end gap-4 flex-col-reverse sm:flex-row sm:items-center sm:gap-6"
                    >
                      <img
                        class="hidden sm:inline-block h-6 w-6 rounded-full ring-2 ring-white"
                        src="https://ryanc.cc/avatar"
                      />
                      <time class="text-sm text-gray-500" datetime="2020-01-07">
                        2020-01-07
                      </time>
                      <span class="cursor-pointer">
                        <IconSettings @click.stop="detailVisible = true" />
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </li>
          </ul>

          <template #footer>
            <div class="bg-white flex items-center justify-end">
              <div class="flex-1 flex items-center justify-end">
                <div>
                  <nav
                    aria-label="Pagination"
                    class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px"
                  >
                    <a
                      class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
                      href="#"
                    >
                      <span class="sr-only">Previous</span>
                      <svg
                        aria-hidden="true"
                        class="h-5 w-5"
                        fill="currentColor"
                        viewBox="0 0 20 20"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path
                          clip-rule="evenodd"
                          d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z"
                          fill-rule="evenodd"
                        />
                      </svg>
                    </a>
                    <a
                      aria-current="page"
                      class="z-10 bg-indigo-50 border-indigo-500 text-indigo-600 relative inline-flex items-center px-4 py-2 border text-sm font-medium"
                      href="#"
                    >
                      1
                    </a>
                    <a
                      class="bg-white border-gray-300 text-gray-500 hover:bg-gray-50 relative inline-flex items-center px-4 py-2 border text-sm font-medium"
                      href="#"
                    >
                      2
                    </a>
                    <span
                      class="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-700"
                    >
                      ...
                    </span>
                    <a
                      class="bg-white border-gray-300 text-gray-500 hover:bg-gray-50 hidden md:inline-flex relative items-center px-4 py-2 border text-sm font-medium"
                      href="#"
                    >
                      4
                    </a>
                    <a
                      class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
                      href="#"
                    >
                      <span class="sr-only">Next</span>
                      <svg
                        aria-hidden="true"
                        class="h-5 w-5"
                        fill="currentColor"
                        viewBox="0 0 20 20"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path
                          clip-rule="evenodd"
                          d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"
                          fill-rule="evenodd"
                        />
                      </svg>
                    </a>
                  </nav>
                </div>
              </div>
            </div>
          </template>
        </VCard>
      </div>
    </div>
  </div>
</template>
