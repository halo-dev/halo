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
  IconArrowLeft,
  IconArrowRight,
  IconBookRead,
  IconSettings,
} from "@/core/icons";
import { posts } from "./posts-mock";
import { ref } from "vue";
import type { Post } from "@halo-dev/admin-api";

const postsRef = ref(
  // eslint-disable-next-line
  posts.map((item: any) => {
    return {
      ...item,
      checked: false,
    };
  })
);

const checkAll = ref(false);
const postSettings = ref(false);
const settingActiveId = ref("general");
// eslint-disable-next-line
const selected = ref<Post | Record<string, any>>({});
const saving = ref(false);

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
handleCheckAll();
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
          <div class="divide-y-0 sm:divide-y sm:divide-gray-200 space-y-6">
            <div class="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                标题
              </label>
              <div class="mt-1 sm:mt-0 sm:col-span-2">
                <VInput v-model="selected.title"></VInput>
              </div>
            </div>

            <div class="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                别名
              </label>
              <div class="mt-1 sm:mt-0 sm:col-span-2">
                <VInput v-model="selected.slug"></VInput>
              </div>
            </div>

            <div
              class="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-center sm:pt-5"
            >
              <label class="block text-sm font-medium text-gray-700">
                分类目录
              </label>
              <div class="mt-1 sm:mt-0 sm:col-span-2">
                <VInput></VInput>
              </div>
            </div>

            <div class="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                标签
              </label>
              <div class="mt-1 sm:mt-0 sm:col-span-2">
                <VInput></VInput>
              </div>
            </div>
            <div class="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                摘要
              </label>
              <div class="mt-1 sm:mt-0 sm:col-span-2">
                <VTextarea v-model="selected.summary"></VTextarea>
              </div>
            </div>
          </div>
        </form>
      </VTabItem>
      <VTabItem id="advanced" label="高级">
        <form>
          <div class="divide-y-0 sm:divide-y sm:divide-gray-200 space-y-6">
            <div class="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                禁止评论
              </label>
              <div class="mt-1 sm:mt-0 sm:col-span-2">
                <VInput v-model="selected.disallowComment"></VInput>
              </div>
            </div>

            <div class="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                是否置顶
              </label>
              <div class="mt-1 sm:mt-0 sm:col-span-2">
                <VInput v-model="selected.topPriority"></VInput>
              </div>
            </div>
            <div class="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                发表时间
              </label>
              <div class="mt-1 sm:mt-0 sm:col-span-2">
                <VInput></VInput>
              </div>
            </div>
            <div class="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                自定义模板
              </label>
              <div class="mt-1 sm:mt-0 sm:col-span-2">
                <VInput v-model="selected.template"></VInput>
              </div>
            </div>
            <div class="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                访问密码
              </label>
              <div class="mt-1 sm:mt-0 sm:col-span-2">
                <VInput v-model="selected.password"></VInput>
              </div>
            </div>
            <div class="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                封面图
              </label>
              <div class="mt-1 sm:mt-0 sm:col-span-2">
                <VSpace align="start" class="w-full" direction="column">
                  <div class="w-full sm:w-1/2">
                    <div
                      class="block aspect-w-10 aspect-h-7 bg-gray-100 overflow-hidden cursor-pointer rounded"
                    >
                      <img
                        :src="selected.thumbnail"
                        alt=""
                        class="object-cover pointer-events-none"
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
          <div class="divide-y-0 sm:divide-y sm:divide-gray-200 space-y-6">
            <div class="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                自定义关键词
              </label>
              <div class="mt-1 sm:mt-0 sm:col-span-2">
                <VTextarea
                  v-model="selected.metaKeywords"
                  :rows="5"
                ></VTextarea>
              </div>
            </div>

            <div class="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                自定义描述
              </label>
              <div class="mt-1 sm:mt-0 sm:col-span-2">
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
          <div class="divide-y-0 sm:divide-y sm:divide-gray-200 space-y-6">
            <div class="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                CSS
              </label>
              <div class="mt-1 sm:mt-0 sm:col-span-2">
                <VTextarea :rows="5"></VTextarea>
              </div>
            </div>

            <div class="sm:grid sm:grid-cols-3 sm:gap-4 sm:items-start sm:pt-5">
              <label
                class="block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2"
              >
                JavaScript
              </label>
              <div class="mt-1 sm:mt-0 sm:col-span-2">
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
      <IconBookRead class="self-center mr-2" />
    </template>
    <template #actions>
      <VButton :route="{ name: 'PostEditor' }" type="secondary">发布</VButton>
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <template #header>
        <div
          class="flex flex-col w-full p-4 items-stretch sm:flex-row sm:items-center"
        >
          <div class="flex-1">
            <VInput placeholder="输入关键词搜索" />
          </div>
          <div class="flex flex-row gap-3">
            <div>分类</div>
            <div>标签</div>
            <div>作者</div>
            <div>排序</div>
          </div>
        </div>
      </template>
      <ul class="divide-y divide-gray-100 box-border w-full h-full" role="list">
        <li v-for="(post, index) in postsRef" :key="index">
          <div
            :class="{
              'border-l-2 border-themeable-primary bg-gray-100':
                selected.id === post.id,
            }"
            class="px-4 py-3 block hover:bg-gray-50 cursor-pointer transition-all"
          >
            <div class="flex flex-row items-center relative">
              <div class="flex-1">
                <div class="flex flex-col sm:flex-row">
                  <span
                    class="mr-0 sm:mr-2 text-sm font-medium truncate text-gray-900"
                  >
                    {{ post.title }}
                  </span>
                  <VSpace class="mt-1 sm:mt-0">
                    <VTag v-for="(tag, tagIndex) in post.tags" :key="tagIndex">
                      {{ tag.name }}
                    </VTag>
                  </VSpace>
                </div>
                <div class="flex mt-1">
                  <VSpace>
                    <span class="text-xs text-gray-500"
                      >阅读 {{ post.visits }}</span
                    >
                    <span class="text-xs text-gray-500"
                      >评论 {{ post.commentCount }}</span
                    >
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
                    <IconSettings @click="handleSelect(post)" />
                  </span>
                </div>
              </div>
            </div>
          </div>
        </li>
      </ul>

      <template #footer>
        <div class="bg-white flex items-center justify-between">
          <div class="flex-1 flex justify-between sm:hidden">
            <a
              class="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
              href="#"
            >
              Previous
            </a>
            <a
              class="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
              href="#"
            >
              Next
            </a>
          </div>
          <div
            class="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between"
          >
            <div>
              <p class="text-sm text-gray-700">
                Showing
                <span class="font-medium">1</span>
                to
                <span class="font-medium">10</span>
                of
                <span class="font-medium">97</span>
                results
              </p>
            </div>
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
                <a
                  class="bg-white border-gray-300 text-gray-500 hover:bg-gray-50 hidden md:inline-flex relative items-center px-4 py-2 border text-sm font-medium"
                  href="#"
                >
                  3
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
                  8
                </a>
                <a
                  class="bg-white border-gray-300 text-gray-500 hover:bg-gray-50 relative inline-flex items-center px-4 py-2 border text-sm font-medium"
                  href="#"
                >
                  9
                </a>
                <a
                  class="bg-white border-gray-300 text-gray-500 hover:bg-gray-50 relative inline-flex items-center px-4 py-2 border text-sm font-medium"
                  href="#"
                >
                  10
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
</template>

<style lang="scss">
.table-container {
  @apply table;
  @apply w-full;
  border: 1px solid #fff;
  background: #fff;

  border-radius: 4px;

  .table-item {
    @apply table-row;
    @apply cursor-pointer;
    @apply transition-all;

    &:hover {
      @apply bg-gray-100;
    }
  }

  .table-cell {
    display: table-cell;
    padding: 10px;
  }
}
</style>
