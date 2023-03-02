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
import { ref, watch } from "vue";
import type { ListedPost, Post } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { formatDatetime } from "@/utils/date";
import { usePermission } from "@/utils/permission";
import cloneDeep from "lodash.clonedeep";
import { getNode } from "@formkit/core";
import FilterTag from "@/components/filter/FilterTag.vue";
import { useQuery } from "@tanstack/vue-query";

const { currentUserHasPermission } = usePermission();

const checkedAll = ref(false);
const selectedPostNames = ref<string[]>([]);
const keyword = ref("");

const page = ref(1);
const size = ref(20);
const total = ref(0);

const {
  data: posts,
  isLoading,
  isFetching,
  refetch,
} = useQuery<ListedPost[]>({
  queryKey: ["deleted-posts", page, size, keyword],
  queryFn: async () => {
    const { data } = await apiClient.post.listPosts({
      labelSelector: [`content.halo.run/deleted=true`],
      page: page.value,
      size: size.value,
      keyword: keyword.value,
    });

    total.value = data.total;

    return data.items;
  },
  refetchOnWindowFocus: false,
  refetchInterval: (data) => {
    const deletingPosts = data?.filter(
      (post) =>
        !!post.post.metadata.deletionTimestamp || !post.post.spec.deleted
    );
    return deletingPosts?.length ? 3000 : false;
  },
});

const checkSelection = (post: Post) => {
  return selectedPostNames.value.includes(post.metadata.name);
};

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;

  if (checked) {
    selectedPostNames.value =
      posts.value?.map((post) => {
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
      await refetch();

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
      await refetch();
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

      await refetch();

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
          const post = posts.value?.find(
            (item) => item.post.metadata.name === name
          )?.post;

          if (!post) {
            return Promise.resolve();
          }

          return apiClient.extension.post.updatecontentHaloRunV1alpha1Post({
            name: post.metadata.name,
            post: {
              ...post,
              spec: {
                ...post.spec,
                deleted: false,
              },
            },
          });
        })
      );
      await refetch();
      selectedPostNames.value = [];

      Toast.success("恢复成功");
    },
  });
};

watch(selectedPostNames, (newValue) => {
  checkedAll.value = newValue.length === posts.value?.length;
});

function handleKeywordChange() {
  const keywordNode = getNode("keywordInput");
  if (keywordNode) {
    keyword.value = keywordNode._value as string;
  }
  page.value = 1;
}

function handleClearKeyword() {
  keyword.value = "";
  page.value = 1;
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
                    @click="refetch()"
                  >
                    <IconRefreshLine
                      :class="{ 'animate-spin text-gray-900': isFetching }"
                      class="h-4 w-4 text-gray-600 group-hover:text-gray-900"
                    />
                  </div>
                </div>
              </VSpace>
            </div>
          </div>
        </div>
      </template>

      <VLoading v-if="isLoading" />

      <Transition v-else-if="!posts?.length" appear name="fade">
        <VEmpty
          message="你可以尝试刷新或者返回文章管理"
          title="没有文章被放入回收站"
        >
          <template #actions>
            <VSpace>
              <VButton @click="refetch">刷新</VButton>
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
          <li v-for="(post, index) in posts" :key="index">
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
                  <template #description>
                    <div class="flex flex-col gap-1.5">
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
                      <VSpace v-if="post.tags.length" class="flex-wrap">
                        <PostTag
                          v-for="(tag, tagIndex) in post.tags"
                          :key="tagIndex"
                          :tag="tag"
                          route
                        ></PostTag>
                      </VSpace>
                    </div>
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
            v-model:page="page"
            v-model:size="size"
            :total="total"
            :size-options="[20, 30, 50, 100]"
          />
        </div>
      </template>
    </VCard>
  </div>
</template>
