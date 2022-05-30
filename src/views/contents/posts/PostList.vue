<script lang="ts" setup>
import { VButton } from "@/components/base/button";
import { VCard } from "@/components/base/card";
import { VSpace } from "@/components/base/space";
import { VTag } from "@/components/base/tag";
import { VInput } from "@/components/base/input";
import { VPageHeader } from "@/components/base/header";
import { VModal } from "@/components/base/modal";
import { VTabItem, VTabs } from "@/components/base/tabs";
import { VTextarea } from "@/components/base/textarea";
import {
  IconAddCircle,
  IconArrowDown,
  IconArrowLeft,
  IconArrowRight,
  IconBookRead,
  IconDeleteBin,
  IconSettings,
} from "@/core/icons";
import { posts } from "./posts-mock";
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import type { Post } from "@halo-dev/admin-api";
import { users } from "@/views/system/users/users-mock";

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
const settingActiveId = ref("general");
// eslint-disable-next-line
const selected = ref<Post | Record<string, any>>({});
const saving = ref(false);

const checkedCount = computed(() => {
  return postsRef.value.filter((post) => post.checked).length;
});

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
  const currentIndex = posts.findIndex((post) => post.id === selected.value.id);
  if (currentIndex > 0) {
    selected.value = posts[currentIndex - 1];
  }
};

const handleSelectNext = () => {
  const currentIndex = posts.findIndex((post) => post.id === selected.value.id);
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
</script>
<template>
  <VModal v-model:visible="postSettings" :width="680" title="文章设置">
    <template #actions>
      <div class="modal-header-action" @click="handleSelectPrevious">
        <IconArrowLeft />
      </div>
      <div class="modal-header-action" @click="handleSelectNext">
        <IconArrowRight />
      </div>
    </template>

    <VTabs v-model:active-id="settingActiveId" type="outline">
      <VTabItem id="general" label="常规">
        <form>
          <div class="space-y-6 divide-y-0 sm:divide-y sm:divide-gray-200">
            <div class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                标题
              </label>
              <div class="mt-1 sm:col-span-2 sm:mt-0">
                <VInput v-model="selected.title"></VInput>
              </div>
            </div>

            <div class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                别名
              </label>
              <div class="mt-1 sm:col-span-2 sm:mt-0">
                <VInput v-model="selected.slug"></VInput>
              </div>
            </div>

            <div
              class="sm:grid sm:grid-cols-3 sm:items-center sm:gap-4 sm:pt-5"
            >
              <label class="block text-sm font-medium text-gray-700">
                分类目录
              </label>
              <div class="mt-1 sm:col-span-2 sm:mt-0">
                <VInput></VInput>
              </div>
            </div>

            <div class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                标签
              </label>
              <div class="mt-1 sm:col-span-2 sm:mt-0">
                <VInput></VInput>
              </div>
            </div>
            <div class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                摘要
              </label>
              <div class="mt-1 sm:col-span-2 sm:mt-0">
                <VTextarea v-model="selected.summary"></VTextarea>
              </div>
            </div>
          </div>
        </form>
      </VTabItem>
      <VTabItem id="advanced" label="高级">
        <form>
          <div class="space-y-6 divide-y-0 sm:divide-y sm:divide-gray-200">
            <div class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                禁止评论
              </label>
              <div class="mt-1 sm:col-span-2 sm:mt-0">
                <VInput v-model="selected.disallowComment"></VInput>
              </div>
            </div>

            <div class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                是否置顶
              </label>
              <div class="mt-1 sm:col-span-2 sm:mt-0">
                <VInput v-model="selected.topPriority"></VInput>
              </div>
            </div>
            <div class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                发表时间
              </label>
              <div class="mt-1 sm:col-span-2 sm:mt-0">
                <VInput></VInput>
              </div>
            </div>
            <div class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                自定义模板
              </label>
              <div class="mt-1 sm:col-span-2 sm:mt-0">
                <VInput v-model="selected.template"></VInput>
              </div>
            </div>
            <div class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                访问密码
              </label>
              <div class="mt-1 sm:col-span-2 sm:mt-0">
                <VInput v-model="selected.password"></VInput>
              </div>
            </div>
            <div class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                封面图
              </label>
              <div class="mt-1 sm:col-span-2 sm:mt-0">
                <VSpace align="start" class="w-full" direction="column">
                  <div class="w-full sm:w-1/2">
                    <div
                      class="aspect-w-10 aspect-h-7 block cursor-pointer overflow-hidden rounded bg-gray-100"
                    >
                      <img
                        :src="selected.thumbnail"
                        alt=""
                        class="pointer-events-none object-cover"
                      />
                    </div>
                  </div>
                  <VInput v-model="selected.thumbnail"></VInput>
                </VSpace>
              </div>
            </div>
          </div>
        </form>
      </VTabItem>
      <VTabItem id="seo" label="SEO">
        <form>
          <div class="space-y-6 divide-y-0 sm:divide-y sm:divide-gray-200">
            <div class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                自定义关键词
              </label>
              <div class="mt-1 sm:col-span-2 sm:mt-0">
                <VTextarea
                  v-model="selected.metaKeywords"
                  :rows="5"
                ></VTextarea>
              </div>
            </div>

            <div class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                自定义描述
              </label>
              <div class="mt-1 sm:col-span-2 sm:mt-0">
                <VTextarea
                  v-model="selected.metaDescription"
                  :rows="5"
                ></VTextarea>
              </div>
            </div>
          </div>
        </form>
      </VTabItem>
      <VTabItem id="metas" label="元数据"></VTabItem>
      <VTabItem id="inject-code" label="代码注入">
        <form>
          <div class="space-y-6 divide-y-0 sm:divide-y sm:divide-gray-200">
            <div class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                CSS
              </label>
              <div class="mt-1 sm:col-span-2 sm:mt-0">
                <VTextarea :rows="5"></VTextarea>
              </div>
            </div>

            <div class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                JavaScript
              </label>
              <div class="mt-1 sm:col-span-2 sm:mt-0">
                <VTextarea :rows="5"></VTextarea>
              </div>
            </div>
          </div>
        </form>
      </VTabItem>
    </VTabs>

    <template #footer>
      <VSpace>
        <VButton :loading="saving" type="secondary" @click="saving = !saving">
          保存
        </VButton>
        <VButton type="default" @click="postSettings = false">取消</VButton>
      </VSpace>
    </template>
  </VModal>
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
              <VInput
                v-if="checkedCount <= 0"
                class="w-full sm:w-72"
                placeholder="输入关键词搜索"
              />
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
                      <VInput placeholder="根据关键词搜索"></VInput>
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
                      <VInput placeholder="根据关键词搜索"></VInput>
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
                        <VInput placeholder="根据关键词搜索"></VInput>
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
                                  :alt="user.name"
                                  :src="user.avatar"
                                  class="h-10 w-10 rounded"
                                />
                              </div>
                              <div class="min-w-0 flex-1">
                                <p
                                  class="truncate text-sm font-medium text-gray-900"
                                >
                                  {{ user.name }}
                                </p>
                                <p class="truncate text-sm text-gray-500">
                                  @{{ user.username }}
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
        <li
          v-for="(post, index) in postsRef"
          :key="index"
          @click="handleRouteToEditor(post)"
        >
          <div
            :class="{
              'bg-gray-100': selected.id === post.id || post.checked,
            }"
            class="relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
          >
            <div
              v-show="selected.id === post.id || post.checked"
              class="absolute inset-y-0 left-0 w-0.5 bg-themeable-primary"
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
        <div class="flex items-center justify-end bg-white">
          <div class="flex flex-1 items-center justify-end">
            <div>
              <nav
                aria-label="Pagination"
                class="relative z-0 inline-flex -space-x-px rounded-md shadow-sm"
              >
                <a
                  class="relative inline-flex items-center rounded-l-md border border-gray-300 bg-white px-2 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50"
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
                  class="relative z-10 inline-flex items-center border border-indigo-500 bg-indigo-50 px-4 py-2 text-sm font-medium text-indigo-600"
                  href="#"
                >
                  1
                </a>
                <a
                  class="relative inline-flex items-center border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50"
                  href="#"
                >
                  2
                </a>
                <span
                  class="relative inline-flex items-center border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700"
                >
                  ...
                </span>
                <a
                  class="relative hidden items-center border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50 md:inline-flex"
                  href="#"
                >
                  4
                </a>
                <a
                  class="relative inline-flex items-center rounded-r-md border border-gray-300 bg-white px-2 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50"
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
</template>
