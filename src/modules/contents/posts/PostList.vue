<script lang="ts" setup>
import {
  IconAddCircle,
  IconArrowDown,
  IconBookRead,
  IconDeleteBin,
  IconSettings,
  VButton,
  VCard,
  VPageHeader,
  VPagination,
  VSpace,
  VTag,
} from "@halo-dev/components";
import PostSettingModal from "./components/PostSettingModal.vue";
import { posts } from "./posts-mock";
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import type { Post } from "@halo-dev/admin-api";
import { apiClient } from "@halo-dev/admin-shared";
import type { User } from "@halo-dev/api-client";

const postsRef = ref(
  // eslint-disable-next-line
  posts.map((item: any) => {
    return {
      ...item,
      checked: false,
    };
  })
);

const router = useRouter();
const checkAll = ref(false);
const postSettings = ref(false);
// eslint-disable-next-line
const selected = ref<Post | Record<string, unknown> | null>({});
const users = ref<User[]>([]);

const checkedCount = computed(() => {
  return postsRef.value.filter((post) => post.checked).length;
});

const handleFetchUsers = async () => {
  try {
    const { data } = await apiClient.extension.user.listv1alpha1User();
    users.value = data.items;
  } catch (e) {
    console.error(e);
  }
};

const handleCheckAll = () => {
  postsRef.value.forEach((item) => {
    item.checked = checkAll.value;
  });
};

// eslint-disable-next-line
const handleSelect = (post: any) => {
  selected.value = post;
  postSettings.value = true;
};

const handleSelectPrevious = () => {
  const currentIndex = posts.findIndex(
    (post) => post.id === selected.value?.id
  );
  if (currentIndex > 0) {
    selected.value = posts[currentIndex - 1];
  }
};

const handleSelectNext = () => {
  const currentIndex = posts.findIndex(
    (post) => post.id === selected.value?.id
  );
  if (currentIndex < posts.length - 1) {
    selected.value = posts[currentIndex + 1];
  }
};

// eslint-disable-next-line
const handleRouteToEditor = (post: any) => {
  router.push({
    name: "PostEditor",
    params: {
      id: post.id,
    },
  });
};

onMounted(() => {
  handleFetchUsers();
});
</script>
<template>
  <PostSettingModal
    v-model:visible="postSettings"
    :post="selected"
    @next="handleSelectNext"
    @previous="handleSelectPrevious"
  />
  <VPageHeader title="文章">
    <template #icon>
      <IconBookRead class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <VButton size="sm">
          <template #icon>
            <IconDeleteBin class="h-full w-full" />
          </template>
          回收站
        </VButton>
        <VButton :route="{ name: 'PostEditor' }" type="secondary">
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
            <div class="mr-4 hidden items-center sm:flex">
              <input
                v-model="checkAll"
                class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                type="checkbox"
                @change="handleCheckAll()"
              />
            </div>
            <div class="flex w-full flex-1 sm:w-auto">
              <FormKit
                v-if="checkedCount <= 0"
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
                    <span class="mr-0.5">状态</span>
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
                          <span class="truncate">全部</span>
                        </li>
                        <li
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">已发布</span>
                        </li>
                        <li
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">草稿</span>
                        </li>
                        <li
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">未审核</span>
                        </li>
                      </ul>
                    </div>
                  </template>
                </FloatingDropdown>
                <FloatingDropdown>
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">分类</span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <div class="h-96 w-80 p-4">
                      <FormKit
                        placeholder="输入关键词搜索"
                        type="text"
                      ></FormKit>
                    </div>
                  </template>
                </FloatingDropdown>
                <FloatingDropdown>
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">标签</span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <div class="h-96 w-80 p-4">
                      <FormKit
                        placeholder="输入关键词搜索"
                        type="text"
                      ></FormKit>
                    </div>
                  </template>
                </FloatingDropdown>
                <FloatingDropdown>
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">作者</span>
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
                          <span class="truncate">较近发布</span>
                        </li>
                        <li
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">较晚发布</span>
                        </li>
                        <li
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">浏览量最多</span>
                        </li>
                        <li
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">浏览量最少</span>
                        </li>
                        <li
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">评论量最多</span>
                        </li>
                        <li
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">评论量最少</span>
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
      <ul class="box-border h-full w-full divide-y divide-gray-100" role="list">
        <li v-for="(post, index) in postsRef" :key="index">
          <div
            :class="{
              'bg-gray-100': selected?.id === post.id || post.checked,
            }"
            class="relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
          >
            <div
              v-show="selected?.id === post.id || post.checked"
              class="absolute inset-y-0 left-0 w-0.5 bg-primary"
            ></div>
            <div class="relative flex flex-row items-center">
              <div class="mr-4 hidden items-center sm:flex">
                <input
                  v-model="post.checked"
                  class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                  type="checkbox"
                />
              </div>
              <div class="flex-1">
                <div class="flex flex-col sm:flex-row">
                  <span
                    class="mr-0 truncate text-sm font-medium text-gray-900 sm:mr-2"
                    @click="handleRouteToEditor(post)"
                  >
                    {{ post.title }}
                  </span>
                  <VSpace class="mt-1 sm:mt-0">
                    <VTag v-for="(tag, tagIndex) in post.tags" :key="tagIndex">
                      {{ tag.name }}
                    </VTag>
                  </VSpace>
                </div>
                <div class="mt-1 flex">
                  <VSpace>
                    <span class="text-xs text-gray-500"
                      >访问量 {{ post.visits }}</span
                    >
                    <span class="text-xs text-gray-500"
                      >评论 {{ post.commentCount }}</span
                    >
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
                    <IconSettings @click.stop="handleSelect(post)" />
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
</template>
