<script lang="ts" setup>
import { VPageHeader } from "@/components/base/header";
import { VButton } from "@/components/base/button";
import { VModal } from "@/components/base/modal";
import { VCard } from "@/components/base/card";
import { VSpace } from "@/components/base/space";
import { ref } from "vue";
import {
  IconArrowLeft,
  IconArrowRight,
  IconPalette,
  IconSettings,
} from "@/core/icons";

const strategyVisible = ref(false);
const attachmentSelectVisible = ref(false);
const uploadVisible = ref(false);
const detailVisible = ref(false);

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

  <VModal v-model:visible="detailVisible" title="attachment-0" :width="1000">
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

  <div class="m-4">
    <div class="flex-col flex sm:flex-row gap-2">
      <div class="w-full">
        <ul
          class="grid grid-cols-2 gap-x-2 gap-y-3 sm:grid-cols-3 md:grid-cols-2 xl:grid-cols-8 2xl:grid-cols-12"
          role="list"
        >
          <li
            v-for="(attachment, index) in attachments"
            :key="index"
            class="relative"
            @click="detailVisible = true"
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
    </div>
  </div>
</template>
