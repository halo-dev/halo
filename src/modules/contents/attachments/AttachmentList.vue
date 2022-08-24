<script lang="ts" setup>
import {
  IconAddCircle,
  IconArrowDown,
  IconCheckboxFill,
  IconDatabase2Line,
  IconGrid,
  IconList,
  IconMore,
  IconPalette,
  IconSettings,
  IconUpload,
  VButton,
  VCard,
  VPageHeader,
  VPagination,
  VSpace,
  VTag,
} from "@halo-dev/components";
import AttachmentDetailModal from "./components/AttachmentDetailModal.vue";
import AttachmentUploadModal from "./components/AttachmentUploadModal.vue";
import AttachmentSelectModal from "./components/AttachmentSelectModal.vue";
import AttachmentStrategiesModal from "./components/AttachmentStrategiesModal.vue";
import AttachmentGroupEditingModal from "./components/AttachmentGroupEditingModal.vue";
import { ref } from "vue";
import { useUserFetch } from "@/modules/system/users/composables/use-user";

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
const selectVisible = ref(false);
const uploadVisible = ref(false);
const detailVisible = ref(false);
const groupEditingModal = ref(false);
const checkAll = ref(false);

const { users } = useUserFetch();

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

const folders = [
  {
    name: "2022",
  },
  {
    name: "2021",
  },
  {
    name: "Photos",
  },
  {
    name: "Documents",
  },
  {
    name: "Videos",
  },
  {
    name: "Pictures",
  },
  {
    name: "Developer",
  },
];
</script>
<template>
  <AttachmentDetailModal v-model:visible="detailVisible" />
  <AttachmentUploadModal v-model:visible="uploadVisible" />
  <AttachmentSelectModal v-model:visible="selectVisible" />
  <AttachmentStrategiesModal v-model:visible="strategyVisible" />
  <AttachmentGroupEditingModal v-model:visible="groupEditingModal" />
  <VPageHeader title="附件库">
    <template #icon>
      <IconPalette class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <VButton size="sm" @click="strategyVisible = true">
          <template #icon>
            <IconDatabase2Line class="h-full w-full" />
          </template>
          存储策略
        </VButton>
        <VButton size="sm">
          <template #icon>
            <IconSettings class="h-full w-full" />
          </template>
          设置
        </VButton>
        <VButton type="secondary" @click="uploadVisible = true">
          <template #icon>
            <IconUpload class="h-full w-full" />
          </template>
          上传
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>

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
                              v-close-popper
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">本地</span>
                            </li>
                            <li
                              v-close-popper
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">阿里云 OSS</span>
                            </li>
                            <li
                              v-close-popper
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
                                v-close-popper
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
                              v-close-popper
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">未被引用</span>
                            </li>
                            <li
                              v-close-popper
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
                              v-close-popper
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">较近上传</span>
                            </li>
                            <li
                              v-close-popper
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">较晚上传</span>
                            </li>
                            <li
                              v-close-popper
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">文件大小降序</span>
                            </li>
                            <li
                              v-close-popper
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

          <div v-if="viewType === 'grid'">
            <div class="mb-5 grid grid-cols-2 gap-x-2 gap-y-3 sm:grid-cols-6">
              <div
                class="flex cursor-pointer items-center rounded-base bg-gray-200 p-2 text-gray-900 transition-all"
              >
                <div class="flex flex-1 items-center">
                  <span class="text-sm">全部（212）</span>
                </div>
              </div>
              <div
                class="flex cursor-pointer items-center rounded-base bg-gray-100 p-2 text-gray-500 transition-all hover:bg-gray-200 hover:text-gray-900 hover:shadow-sm"
              >
                <div class="flex flex-1 items-center">
                  <span class="text-sm">未分组（18）</span>
                </div>
              </div>
              <div
                v-for="(folder, index) in folders"
                :key="index"
                class="flex cursor-pointer items-center rounded-base bg-gray-100 p-2 text-gray-500 transition-all hover:bg-gray-200 hover:text-gray-900 hover:shadow-sm"
              >
                <div class="flex flex-1 items-center">
                  <span class="text-sm">
                    {{ folder.name }}（{{ index * 20 }}）
                  </span>
                </div>
                <FloatingDropdown>
                  <IconMore />
                  <template #popper>
                    <div class="w-48 p-2">
                      <VSpace class="w-full" direction="column">
                        <VButton
                          v-close-popper
                          block
                          type="secondary"
                          @click="groupEditingModal = true"
                        >
                          重命名
                        </VButton>
                        <VButton v-close-popper block type="danger">
                          删除
                        </VButton>
                      </VSpace>
                    </div>
                  </template>
                </FloatingDropdown>
              </div>
              <div
                class="flex cursor-pointer items-center rounded-base bg-gray-100 p-2 text-gray-500 transition-all hover:bg-gray-200 hover:text-gray-900 hover:shadow-sm"
                @click="groupEditingModal = true"
              >
                <div class="flex flex-1 items-center">
                  <span class="text-sm">添加分组</span>
                </div>
                <IconAddCircle />
              </div>
            </div>
            <div
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
