<script lang="ts" setup>
import {
  IconArrowDown,
  IconArrowLeft,
  IconArrowRight,
  IconCheckboxFill,
  IconGrid,
  IconList,
  IconPalette,
  IconSettings,
  VButton,
  VCard,
  VModal,
  VPageHeader,
  VPagination,
  VSpace,
  VTag,
} from "@halo-dev/components";
import { onMounted, ref } from "vue";
import vueFilePond from "vue-filepond";
import "filepond/dist/filepond.min.css";
import FilePondPluginImagePreview from "filepond-plugin-image-preview";
import "filepond-plugin-image-preview/dist/filepond-plugin-image-preview.min.css";
import type { User } from "@halo-dev/api-client";
import { apiClient } from "@halo-dev/admin-shared";

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

const FilePond = vueFilePond(FilePondPluginImagePreview);

const viewType = ref("grid");

const strategyVisible = ref(false);
const attachmentSelectVisible = ref(false);
const uploadVisible = ref(false);
const detailVisible = ref(false);
const checkAll = ref(false);
const users = ref<User[]>([]);

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

const handleFetchUsers = async () => {
  try {
    const { data } = await apiClient.extension.user.listv1alpha1User();
    users.value = data.items;
  } catch (e) {
    console.error(e);
  }
};

onMounted(() => {
  handleFetchUsers();
});
</script>
<template>
  <VPageHeader title="附件库">
    <template #icon>
      <IconPalette class="mr-2 self-center" />
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

  <VModal
    v-model:visible="uploadVisible"
    :body-class="['!p-0']"
    :width="1024"
    title="上传附件"
  >
    <div class="w-full">
      <div class="flex flex-row">
        <VCard
          :bodyClass="['!p-0']"
          class="border-r-1 w-72 border-t-0 border-l-0 border-b-0"
        >
          <fieldset>
            <div class="-space-y-px divide-y divide-gray-100 bg-white">
              <label
                v-for="(strategy, index) in strategies"
                :key="index"
                :class="{
                  'bg-gray-50': selected === strategy.id,
                }"
                class="relative flex cursor-pointer p-4 focus:outline-none"
              >
                <input
                  v-model="selected"
                  :value="strategy.id"
                  class="mt-0.5 h-4 w-4 shrink-0 cursor-pointer border-gray-300 text-indigo-600 focus:ring-indigo-500"
                  name="privacy"
                  type="radio"
                />
                <span class="ml-3 flex flex-1 flex-col">
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
                    class="cursor-pointer transition-all hover:text-blue-600"
                    @click.stop="strategyVisible = true"
                  />
                </div>
              </label>
            </div>
          </fieldset>

          <template #footer>
            <FloatingDropdown>
              <VButton block type="secondary"> 添加策略</VButton>
              <template #popper>
                <div class="w-72 p-4">
                  <ul class="space-y-1">
                    <li
                      class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                    >
                      <span class="truncate">本地</span>
                    </li>
                    <li
                      class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                      @click="strategyVisible = true"
                    >
                      <span class="truncate">阿里云 OSS</span>
                    </li>
                    <li
                      class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                    >
                      <span class="truncate">Amazon S3</span>
                    </li>
                  </ul>
                </div>
              </template>
            </FloatingDropdown>
          </template>
        </VCard>

        <div class="flex-1 p-4">
          <file-pond
            ref="pond"
            accepted-file-types="image/jpeg, image/png"
            label-idle="Drop files here..."
            name="test"
            server="/api"
            v-bind:allow-multiple="true"
          />
        </div>
      </div>
    </div>
  </VModal>

  <VModal
    v-model:visible="strategyVisible"
    :width="820"
    title="添加存储策略"
  ></VModal>

  <VModal v-model:visible="detailVisible" :width="1000" title="attachment-0">
    <template #actions>
      <div class="modal-header-action">
        <IconArrowLeft />
      </div>
      <div class="modal-header-action">
        <IconArrowRight />
      </div>
    </template>
    <div class="overflow-hidden bg-white">
      <div>
        <dl>
          <div
            class="bg-gray-50 px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">原始内容</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
              <img
                class="w-full rounded sm:w-1/2"
                src="https://picsum.photos/1000/700?random=1"
              />
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">存储策略</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
              阿里云/bucket/blog-attachments
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">文件名称</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
              attachment-0
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">文件类型</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
              image/jpeg
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">文件大小</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
              1.2 MB
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">上传者</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
              Ryan Wang
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">上传时间</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
              2020-01-01 12:00:00
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">原始链接</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
              https://picsum.photos/1000/700?random=1
            </dd>
          </div>
          <div
            class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
          >
            <dt class="text-sm font-medium text-gray-900">引用位置</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
              <ul class="mt-2 space-y-2">
                <li>
                  <div
                    class="inline-flex w-96 cursor-pointer flex-row gap-x-3 rounded border p-3 hover:border-primary"
                  >
                    <RouterLink
                      :to="{
                        name: 'Posts',
                      }"
                      class="font-medium text-gray-900 hover:text-blue-400"
                    >
                      Halo 1.5.3 发布了
                    </RouterLink>
                    <div class="text-xs">
                      <VSpace>
                        <VTag>文章</VTag>
                      </VSpace>
                    </div>
                  </div>
                </li>
                <li>
                  <div
                    class="inline-flex w-96 cursor-pointer flex-row gap-x-3 rounded border p-3 hover:border-primary"
                  >
                    <RouterLink
                      :to="{
                        name: 'Posts',
                      }"
                      class="font-medium text-gray-900 hover:text-blue-400"
                    >
                      Halo 1.5.2 发布
                    </RouterLink>
                    <div class="text-xs">
                      <VSpace>
                        <VTag>文章</VTag>
                      </VSpace>
                    </div>
                  </div>
                </li>
              </ul>
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
              class="group aspect-w-10 aspect-h-7 block w-full cursor-pointer overflow-hidden bg-gray-100"
            >
              <img
                :src="attachment.url"
                alt=""
                class="pointer-events-none object-cover group-hover:opacity-75"
              />
            </div>
            <p
              class="pointer-events-none block truncate px-2 py-1 text-sm font-medium text-gray-700"
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
    <div class="flex flex-col gap-2 sm:flex-row">
      <div class="w-full">
        <VCard :body-class="[viewType === 'list' ? '!p-0' : '']">
          <template #header>
            <div class="block w-full bg-gray-50 px-4 py-3">
              <div
                class="relative flex flex-col items-start sm:flex-row sm:items-center"
              >
                <div class="mr-4 hidden items-center sm:flex">
                  <input
                    v-model="checkAll"
                    class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                    type="checkbox"
                  />
                </div>
                <div class="flex w-full flex-1 sm:w-auto">
                  <FormKit
                    v-if="!checkAll"
                    placeholder="输入关键词搜索"
                    type="text"
                  ></FormKit>
                  <VSpace v-else>
                    <VButton type="default">设置</VButton>
                    <VButton type="danger">删除</VButton>
                  </VSpace>
                </div>
                <div class="mt-4 flex sm:mt-0">
                  <VSpace spacing="lg">
                    <FloatingDropdown>
                      <div
                        class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                      >
                        <span class="mr-0.5">存储策略</span>
                        <span>
                          <IconArrowDown />
                        </span>
                      </div>
                      <template #popper>
                        <div class="w-72 p-4">
                          <ul class="space-y-1">
                            <li
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">本地</span>
                            </li>
                            <li
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">阿里云 OSS</span>
                            </li>
                            <li
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">Amazon S3</span>
                            </li>
                          </ul>
                        </div>
                      </template>
                    </FloatingDropdown>
                    <FloatingDropdown>
                      <div
                        class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                      >
                        <span class="mr-0.5">上传者</span>
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
                              <li
                                v-for="(user, index) in users"
                                :key="index"
                                class="cursor-pointer py-4 hover:bg-gray-50"
                              >
                                <div class="flex items-center space-x-4">
                                  <div class="flex items-center">
                                    <input
                                      class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                                      type="checkbox"
                                    />
                                  </div>
                                  <div class="flex-shrink-0">
                                    <img
                                      :alt="user.spec.displayName"
                                      :src="user.spec.avatar"
                                      class="h-10 w-10 rounded"
                                    />
                                  </div>
                                  <div class="min-w-0 flex-1">
                                    <p
                                      class="truncate text-sm font-medium text-gray-900"
                                    >
                                      {{ user.spec.displayName }}
                                    </p>
                                    <p class="truncate text-sm text-gray-500">
                                      @{{ user.metadata.name }}
                                    </p>
                                  </div>
                                  <div>
                                    <VTag>{{ index + 1 }} 篇</VTag>
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
                        <span class="mr-0.5">引用位置</span>
                        <span>
                          <IconArrowDown />
                        </span>
                      </div>
                      <template #popper>
                        <div class="w-72 p-4">
                          <ul class="space-y-1">
                            <li
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">未被引用</span>
                            </li>
                            <li
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">文章</span>
                            </li>
                          </ul>
                        </div>
                      </template>
                    </FloatingDropdown>
                    <div
                      class="flex cursor-pointer items-center text-sm text-gray-700 hover:text-black"
                    >
                      <span class="mr-0.5">标签</span>
                      <span>
                        <IconArrowDown />
                      </span>
                    </div>
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
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">较近上传</span>
                            </li>
                            <li
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">较晚上传</span>
                            </li>
                            <li
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">文件大小降序</span>
                            </li>
                            <li
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">文件大小升序</span>
                            </li>
                          </ul>
                        </div>
                      </template>
                    </FloatingDropdown>
                    <div class="flex flex-row gap-2">
                      <div
                        v-for="(item, index) in viewTypes"
                        :key="index"
                        :class="{
                          'bg-gray-200 font-bold text-black':
                            viewType === item.name,
                        }"
                        class="cursor-pointer rounded p-1 hover:bg-gray-200"
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
            class="mt-2 grid grid-cols-3 gap-x-2 gap-y-3 sm:grid-cols-3 md:grid-cols-6 xl:grid-cols-8 2xl:grid-cols-12"
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
                  class="group aspect-w-10 aspect-h-8 block h-full w-full cursor-pointer overflow-hidden bg-gray-100"
                >
                  <img
                    :src="attachment.url"
                    alt=""
                    class="pointer-events-none object-cover group-hover:opacity-75"
                  />
                </div>
                <p
                  class="pointer-events-none block truncate px-2 py-1 text-center text-xs font-medium text-gray-700"
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
            class="box-border h-full w-full divide-y divide-gray-100"
            role="list"
          >
            <li v-for="(attachment, index) in attachments" :key="index">
              <div
                :class="{
                  'bg-gray-100': checkAll,
                }"
                class="relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
              >
                <div
                  v-show="checkAll"
                  class="absolute inset-y-0 left-0 w-0.5 bg-primary"
                ></div>
                <div class="relative flex flex-row items-center">
                  <div class="mr-4 hidden items-center sm:flex">
                    <input
                      v-model="checkAll"
                      class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                      type="checkbox"
                    />
                  </div>
                  <div class="flex-1">
                    <div class="flex flex-col sm:flex-row">
                      <span
                        class="mr-0 truncate text-sm font-medium text-gray-900 sm:mr-2"
                      >
                        {{ attachment.name }}
                      </span>
                    </div>
                    <div class="mt-1 flex">
                      <VSpace>
                        <span class="text-xs text-gray-500">image/png</span>
                        <span class="text-xs text-gray-500">1.2 MB</span>
                      </VSpace>
                    </div>
                  </div>
                  <div class="flex">
                    <div
                      class="inline-flex flex-col flex-col-reverse items-end gap-4 sm:flex-row sm:items-center sm:gap-6"
                    >
                      <img
                        class="hidden h-6 w-6 rounded-full ring-2 ring-white sm:inline-block"
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
            <div class="bg-white sm:flex sm:items-center sm:justify-end">
              <VPagination :page="1" :size="10" :total="20" />
            </div>
          </template>
        </VCard>
      </div>
    </div>
  </div>
</template>
