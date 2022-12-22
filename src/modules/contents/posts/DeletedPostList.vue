<script lang="ts" setup>
import {
  IconAddCircle,
  IconDeleteBin,
  IconRefreshLine,
  Dialog,
  VButton,
  VCard,
  VEmpty,
  VPageHeader,
  VPagination,
  VSpace,
  VAvatar,
  VStatusDot,
  VEntity,
  VEntityField,
  VLoading,
  Toast,
} from "@halo-dev/components";
import PostTag from "./tags/components/PostTag.vue";
import { onMounted, ref, watch } from "vue";
import type { ListedPostList, Post } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { formatDatetime } from "@/utils/date";
import { usePermission } from "@/utils/permission";
import { onBeforeRouteLeave } from "vue-router";
import cloneDeep from "lodash.clonedeep";
import { getNode } from "@formkit/core";
import FilterTag from "@/components/filter/FilterTag.vue";

const { currentUserHasPermission } = usePermission();

const posts = ref<ListedPostList>({
  page: 1,
  size: 50,
  total: 0,
  items: [],
  first: true,
  last: false,
  hasNext: false,
  hasPrevious: false,
  totalPages: 0,
});
const loading = ref(false);
const checkedAll = ref(false);
const selectedPostNames = ref<string[]>([]);
const refreshInterval = ref();
const keyword = ref("");

const handleFetchPosts = async (page?: number) => {
  try {
    clearInterval(refreshInterval.value);

    loading.value = true;

    if (page) {
      posts.value.page = page;
    }

    const { data } = await apiClient.post.listPosts({
      labelSelector: [`content.halo.run/deleted=true`],
      page: posts.value.page,
      size: posts.value.size,
      keyword: keyword.value,
    });
    posts.value = data;

    const deletedPosts = posts.value.items.filter(
      (post) =>
        !!post.post.metadata.deletionTimestamp || !post.post.spec.deleted
    );

    if (deletedPosts.length) {
      refreshInterval.value = setInterval(() => {
        handleFetchPosts();
      }, 3000);
    }
  } catch (e) {
    console.error("Failed to fetch deleted posts", e);
  } finally {
    loading.value = false;
  }
};

onBeforeRouteLeave(() => {
  clearInterval(refreshInterval.value);
});

const handlePaginationChange = ({
  page,
  size,
}: {
  page: number;
  size: number;
}) => {
  posts.value.page = page;
  posts.value.size = size;
  handleFetchPosts();
};

const checkSelection = (post: Post) => {
  return selectedPostNames.value.includes(post.metadata.name);
};

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;

  if (checked) {
    selectedPostNames.value =
      posts.value.items.map((post) => {
        return post.post.metadata.name;
      }) || [];
  } else {
    selectedPostNames.value = [];
  }
};

const handleDeletePermanently = async (post: Post) => {
  Dialog.warning({
    title: "确定要永久删除该文章吗？",
    description: "删除之后将无法恢复",
    confirmType: "danger",
    onConfirm: async () => {
      await apiClient.extension.post.deletecontentHaloRunV1alpha1Post({
        name: post.metadata.name,
      });
      await handleFetchPosts();

      Toast.success("删除成功");
    },
  });
};

const handleDeletePermanentlyInBatch = async () => {
  Dialog.warning({
    title: "确定要永久删除选中的文章吗？",
    description: "删除之后将无法恢复",
    confirmType: "danger",
    onConfirm: async () => {
      await Promise.all(
        selectedPostNames.value.map((name) => {
          return apiClient.extension.post.deletecontentHaloRunV1alpha1Post({
            name,
          });
        })
      );
      await handleFetchPosts();
      selectedPostNames.value = [];

      Toast.success("删除成功");
    },
  });
};

const handleRecovery = async (post: Post) => {
  Dialog.warning({
    title: "确定要恢复该文章吗？",
    description: "该操作会将文章恢复到被删除之前的状态",
    onConfirm: async () => {
      const postToUpdate = cloneDeep(post);
      postToUpdate.spec.deleted = false;
      await apiClient.extension.post.updatecontentHaloRunV1alpha1Post({
        name: postToUpdate.metadata.name,
        post: postToUpdate,
      });
      await handleFetchPosts();

      Toast.success("恢复成功");
    },
  });
};

const handleRecoveryInBatch = async () => {
  Dialog.warning({
    title: "确定要恢复选中的文章吗？",
    description: "该操作会将文章恢复到被删除之前的状态",
    onConfirm: async () => {
      await Promise.all(
        selectedPostNames.value.map((name) => {
          const post = posts.value.items.find(
            (item) => item.post.metadata.name === name
          )?.post;

          if (!post) {
            return Promise.resolve();
          }

          post.spec.deleted = false;
          return apiClient.extension.post.updatecontentHaloRunV1alpha1Post({
            name: post.metadata.name,
            post: post,
          });
        })
      );
      await handleFetchPosts();
      selectedPostNames.value = [];

      Toast.success("恢复成功");
    },
  });
};

watch(selectedPostNames, (newValue) => {
  checkedAll.value = newValue.length === posts.value.items?.length;
});

onMounted(() => {
  handleFetchPosts();
});

function handleKeywordChange() {
  const keywordNode = getNode("keywordInput");
  if (keywordNode) {
    keyword.value = keywordNode._value as string;
  }
  handleFetchPosts(1);
}

function handleClearKeyword() {
  keyword.value = "";
  handleFetchPosts(1);
}
</script>
<template>
  <VPageHeader title="文章回收站">
    <template #icon>
      <IconDeleteBin class="mr-2 self-center text-green-600" />
    </template>
    <template #actions>
      <VSpace>
        <VButton :route="{ name: 'Posts' }" size="sm">返回</VButton>
        <VButton
          v-permission="['system:posts:manage']"
          :route="{ name: 'PostEditor' }"
          type="secondary"
        >
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
            <div
              v-permission="['system:posts:manage']"
              class="mr-4 hidden items-center sm:flex"
            >
              <input
                v-model="checkedAll"
                class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                type="checkbox"
                @change="handleCheckAllChange"
              />
            </div>
            <div class="flex w-full flex-1 items-center sm:w-auto">
              <div
                v-if="!selectedPostNames.length"
                class="flex items-center gap-2"
              >
                <FormKit
                  id="keywordInput"
                  outer-class="!p-0"
                  placeholder="输入关键词搜索"
                  type="text"
                  name="keyword"
                  :model-value="keyword"
                  @keyup.enter="handleKeywordChange"
                ></FormKit>

                <FilterTag v-if="keyword" @close="handleClearKeyword()">
                  关键词：{{ keyword }}
                </FilterTag>
              </div>
              <VSpace v-else>
                <VButton type="danger" @click="handleDeletePermanentlyInBatch">
                  永久删除
                </VButton>
                <VButton type="default" @click="handleRecoveryInBatch">
                  恢复
                </VButton>
              </VSpace>
            </div>
            <div class="mt-4 flex sm:mt-0">
              <VSpace spacing="lg">
                <div class="flex flex-row gap-2">
                  <div
                    class="group cursor-pointer rounded p-1 hover:bg-gray-200"
                    @click="handleFetchPosts()"
                  >
                    <IconRefreshLine
                      :class="{ 'animate-spin text-gray-900': loading }"
                      class="h-4 w-4 text-gray-600 group-hover:text-gray-900"
                    />
                  </div>
                </div>
              </VSpace>
            </div>
          </div>
        </div>
      </template>

      <VLoading v-if="loading" />

      <Transition v-else-if="!posts.items.length" appear name="fade">
        <VEmpty
          message="你可以尝试刷新或者返回文章管理"
          title="没有文章被放入回收站"
        >
          <template #actions>
            <VSpace>
              <VButton @click="handleFetchPosts">刷新</VButton>
              <VButton :route="{ name: 'Posts' }" type="primary">
                返回
              </VButton>
            </VSpace>
          </template>
        </VEmpty>
      </Transition>

      <Transition v-else appear name="fade">
        <ul
          class="box-border h-full w-full divide-y divide-gray-100"
          role="list"
        >
          <li v-for="(post, index) in posts.items" :key="index">
            <VEntity :is-selected="checkSelection(post.post)">
              <template
                v-if="currentUserHasPermission(['system:posts:manage'])"
                #checkbox
              >
                <input
                  v-model="selectedPostNames"
                  :value="post.post.metadata.name"
                  class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                  name="post-checkbox"
                  type="checkbox"
                />
              </template>
              <template #start>
                <VEntityField :title="post.post.spec.title" width="27rem">
                  <template #extra>
                    <VSpace class="mt-1 sm:mt-0">
                      <PostTag
                        v-for="(tag, tagIndex) in post.tags"
                        :key="tagIndex"
                        :tag="tag"
                        route
                      ></PostTag>
                    </VSpace>
                  </template>
                  <template #description>
                    <VSpace class="flex-wrap !gap-y-1">
                      <p
                        v-if="post.categories.length"
                        class="inline-flex flex-wrap gap-1 text-xs text-gray-500"
                      >
                        分类：<span
                          v-for="(category, categoryIndex) in post.categories"
                          :key="categoryIndex"
                          class="cursor-pointer hover:text-gray-900"
                        >
                          {{ category.spec.displayName }}
                        </span>
                      </p>
                      <span class="text-xs text-gray-500">
                        访问量 {{ post.stats.visit || 0 }}
                      </span>
                      <span class="text-xs text-gray-500">
                        评论 {{ post.stats.totalComment || 0 }}
                      </span>
                    </VSpace>
                  </template>
                </VEntityField>
              </template>
              <template #end>
                <VEntityField>
                  <template #description>
                    <RouterLink
                      v-for="(
                        contributor, contributorIndex
                      ) in post.contributors"
                      :key="contributorIndex"
                      :to="{
                        name: 'UserDetail',
                        params: { name: contributor.name },
                      }"
                      class="flex items-center"
                    >
                      <VAvatar
                        v-tooltip="contributor.displayName"
                        size="xs"
                        :src="contributor.avatar"
                        :alt="contributor.displayName"
                        circle
                      ></VAvatar>
                    </RouterLink>
                  </template>
                </VEntityField>
                <VEntityField v-if="!post?.post?.spec.deleted">
                  <template #description>
                    <VStatusDot v-tooltip="`恢复中`" state="success" animate />
                  </template>
                </VEntityField>
                <VEntityField v-if="post?.post?.metadata.deletionTimestamp">
                  <template #description>
                    <VStatusDot v-tooltip="`删除中`" state="warning" animate />
                  </template>
                </VEntityField>
                <VEntityField>
                  <template #description>
                    <span class="truncate text-xs tabular-nums text-gray-500">
                      {{ formatDatetime(post.post.spec.publishTime) }}
                    </span>
                  </template>
                </VEntityField>
              </template>
              <template
                v-if="currentUserHasPermission(['system:posts:manage'])"
                #dropdownItems
              >
                <VButton
                  v-close-popper
                  block
                  type="danger"
                  @click="handleDeletePermanently(post.post)"
                >
                  永久删除
                </VButton>
                <VButton
                  v-close-popper
                  block
                  type="default"
                  @click="handleRecovery(post.post)"
                >
                  恢复
                </VButton>
              </template>
            </VEntity>
          </li>
        </ul>
      </Transition>

      <template #footer>
        <div class="bg-white sm:flex sm:items-center sm:justify-end">
          <VPagination
            :page="posts.page"
            :size="posts.size"
            :total="posts.total"
            :size-options="[20, 30, 50, 100]"
            @change="handlePaginationChange"
          />
        </div>
      </template>
    </VCard>
  </div>
</template>
