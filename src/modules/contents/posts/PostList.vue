<script lang="ts" setup>
import {
  IconAddCircle,
  IconArrowDown,
  IconArrowLeft,
  IconArrowRight,
  IconBookRead,
  IconEye,
  IconEyeOff,
  IconSettings,
  IconTeam,
  useDialog,
  VButton,
  VCard,
  VEmpty,
  VPageHeader,
  VPagination,
  VSpace,
  VTag,
} from "@halo-dev/components";
import PostSettingModal from "./components/PostSettingModal.vue";
import PostTag from "../posts/tags/components/PostTag.vue";
import { onMounted, ref, watch, watchEffect } from "vue";
import type { ListedPostList, Post, PostRequest } from "@halo-dev/api-client";
import { apiClient } from "@halo-dev/admin-shared";
import { formatDatetime } from "@/utils/date";
import { useUserFetch } from "@/modules/system/users/composables/use-user";
import { usePostCategory } from "@/modules/contents/posts/categories/composables/use-post-category";
import { usePostTag } from "@/modules/contents/posts/tags/composables/use-post-tag";
import cloneDeep from "lodash.clonedeep";
import { postLabels } from "@/constants/labels";

enum PostPhase {
  DRAFT = "未发布",
  PENDING_APPROVAL = "待审核",
  PUBLISHED = "已发布",
}

const posts = ref<ListedPostList>({
  page: 1,
  size: 20,
  total: 0,
  items: [],
  first: true,
  last: false,
  hasNext: false,
  hasPrevious: false,
});
const loading = ref(false);
const settingModal = ref(false);
const selectedPost = ref<Post | null>(null);
const selectedPostWithContent = ref<PostRequest | null>(null);
const checkedAll = ref(false);
const selectedPostNames = ref<string[]>([]);

const { users } = useUserFetch();
const { categories } = usePostCategory();
const { tags } = usePostTag();
const dialog = useDialog();

const handleFetchPosts = async () => {
  try {
    loading.value = true;

    const labelSelector: string[] = [];

    if (selectedVisibleFilterItem.value.value) {
      labelSelector.push(
        `${postLabels.VISIBLE}=${selectedVisibleFilterItem.value.value}`
      );
    }

    if (selectedPhaseFilterItem.value.value) {
      labelSelector.push(
        `${postLabels.PHASE}=${selectedPhaseFilterItem.value.value}`
      );
    }

    const { data } = await apiClient.post.listPosts(
      posts.value.page,
      posts.value.size,
      labelSelector
    );
    posts.value = data;
  } catch (e) {
    console.error("Failed to fetch posts", e);
  } finally {
    loading.value = false;
  }
};

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

const handleOpenSettingModal = async (post: Post) => {
  const { data } = await apiClient.extension.post.getcontentHaloRunV1alpha1Post(
    post.metadata.name
  );
  selectedPost.value = data;
  settingModal.value = true;
};

const onSettingModalClose = () => {
  selectedPost.value = null;
  selectedPostWithContent.value = null;
  handleFetchPosts();
};

const handleSelectPrevious = async () => {
  const { items, hasPrevious } = posts.value;
  const index = items.findIndex(
    (post) => post.post.metadata.name === selectedPost.value?.metadata.name
  );
  if (index > 0) {
    const { data } =
      await apiClient.extension.post.getcontentHaloRunV1alpha1Post(
        items[index - 1].post.metadata.name
      );
    selectedPost.value = data;
    return;
  }
  if (index === 0 && hasPrevious) {
    posts.value.page--;
    await handleFetchPosts();
    selectedPost.value = posts.value.items[posts.value.items.length - 1].post;
  }
};

const handleSelectNext = async () => {
  const { items, hasNext } = posts.value;
  const index = items.findIndex(
    (post) => post.post.metadata.name === selectedPost.value?.metadata.name
  );
  if (index < items.length - 1) {
    const { data } =
      await apiClient.extension.post.getcontentHaloRunV1alpha1Post(
        items[index + 1].post.metadata.name
      );
    selectedPost.value = data;
    return;
  }
  if (index === items.length - 1 && hasNext) {
    posts.value.page++;
    await handleFetchPosts();
    selectedPost.value = posts.value.items[0].post;
  }
};

const checkSelection = (post: Post) => {
  return (
    post.metadata.name === selectedPost.value?.metadata.name ||
    selectedPostNames.value.includes(post.metadata.name)
  );
};

const finalStatus = (post: Post) => {
  if (post.status?.phase) {
    return PostPhase[post.status.phase];
  }
  return "";
};

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;

  if (checked) {
    selectedPostNames.value =
      posts.value.items.map((post) => {
        return post.post.metadata.name;
      }) || [];
  } else {
    selectedPostNames.value.length = 0;
  }
};

const handleDelete = async (post: Post) => {
  dialog.warning({
    title: "是否确认删除该文章？",
    confirmType: "danger",
    onConfirm: async () => {
      const postToUpdate = cloneDeep(post);
      postToUpdate.spec.deleted = true;
      await apiClient.extension.post.updatecontentHaloRunV1alpha1Post(
        postToUpdate.metadata.name,
        postToUpdate
      );
      await handleFetchPosts();
    },
  });
};

watch(selectedPostNames, (newValue) => {
  checkedAll.value = newValue.length === posts.value.items?.length;
});

watchEffect(async () => {
  if (!selectedPost.value || !selectedPost.value.spec.headSnapshot) {
    return;
  }

  const { data: content } = await apiClient.content.obtainSnapshotContent(
    selectedPost.value.spec.headSnapshot
  );

  selectedPostWithContent.value = {
    post: selectedPost.value,
    content: content,
  };
});

onMounted(() => {
  handleFetchPosts();
});

interface FilterItem {
  label: string;
  value: string | undefined;
}

const VisibleFilterItems: FilterItem[] = [
  {
    label: "全部",
    value: "",
  },
  {
    label: "公开",
    value: "PUBLIC",
  },
  {
    label: "内部成员可访问",
    value: "INTERNAL",
  },
  {
    label: "私有",
    value: "PRIVATE",
  },
];

const PhaseFilterItems: FilterItem[] = [
  {
    label: "全部",
    value: "",
  },
  {
    label: "已发布",
    value: "PUBLISHED",
  },
  {
    label: "未发布",
    value: "DRAFT",
  },
  {
    label: "待审核",
    value: "PENDING_APPROVAL",
  },
];

const selectedVisibleFilterItem = ref<FilterItem>(VisibleFilterItems[0]);
const selectedPhaseFilterItem = ref<FilterItem>(PhaseFilterItems[0]);

function handleVisibleFilterItemChange(filterItem: FilterItem) {
  selectedVisibleFilterItem.value = filterItem;
  handleFetchPosts();
}

function handlePhaseFilterItemChange(filterItem: FilterItem) {
  selectedPhaseFilterItem.value = filterItem;
  handleFetchPosts();
}
</script>
<template>
  <PostSettingModal
    v-model:visible="settingModal"
    :post="selectedPostWithContent"
    @close="onSettingModalClose"
  >
    <template #actions>
      <div class="modal-header-action" @click="handleSelectPrevious">
        <IconArrowLeft />
      </div>
      <div class="modal-header-action" @click="handleSelectNext">
        <IconArrowRight />
      </div>
    </template>
  </PostSettingModal>
  <VPageHeader title="文章">
    <template #icon>
      <IconBookRead class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <VButton :route="{ name: 'Categories' }" size="sm">分类</VButton>
        <VButton :route="{ name: 'Tags' }" size="sm">标签</VButton>
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
                v-model="checkedAll"
                class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                type="checkbox"
                @change="handleCheckAllChange"
              />
            </div>
            <div class="flex w-full flex-1 sm:w-auto">
              <div v-if="!selectedPostNames.length">
                <FormKit placeholder="输入关键词搜索" type="text"></FormKit>
              </div>
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
                          v-for="(filterItem, index) in PhaseFilterItems"
                          :key="index"
                          v-close-popper
                          :class="{
                            'bg-gray-100':
                              selectedPhaseFilterItem.value ===
                              filterItem.value,
                          }"
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                          @click="handlePhaseFilterItemChange(filterItem)"
                        >
                          <span class="truncate">{{ filterItem.label }}</span>
                        </li>
                      </ul>
                    </div>
                  </template>
                </FloatingDropdown>
                <FloatingDropdown>
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5"> 可见性 </span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <div class="w-72 p-4">
                      <ul class="space-y-1">
                        <li
                          v-for="(filterItem, index) in VisibleFilterItems"
                          :key="index"
                          v-close-popper
                          :class="{
                            'bg-gray-100':
                              selectedVisibleFilterItem.value ===
                              filterItem.value,
                          }"
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                          @click="handleVisibleFilterItemChange(filterItem)"
                        >
                          <span class="truncate">
                            {{ filterItem.label }}
                          </span>
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
                    <div class="h-96 w-80">
                      <div class="bg-white p-4">
                        <FormKit
                          placeholder="输入关键词搜索"
                          type="text"
                        ></FormKit>
                      </div>
                      <div class="mt-2">
                        <ul
                          class="box-border h-full w-full divide-y divide-gray-100"
                        >
                          <li
                            v-for="(category, index) in categories"
                            :key="index"
                            v-close-popper
                          >
                            <div
                              class="group relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
                            >
                              <div class="relative flex flex-row items-center">
                                <div class="flex-1">
                                  <div class="flex flex-col sm:flex-row">
                                    <span
                                      class="mr-0 truncate text-sm font-medium text-gray-900 sm:mr-2"
                                    >
                                      {{ category.spec.displayName }}
                                    </span>
                                    <VSpace class="mt-1 sm:mt-0"></VSpace>
                                  </div>
                                  <div class="mt-1 flex">
                                    <span class="text-xs text-gray-500">
                                      /categories/{{ category.spec.slug }}
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
                                  </div>
                                </div>
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
                    <span class="mr-0.5">标签</span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <div class="h-96 w-80">
                      <div class="bg-white p-4">
                        <FormKit
                          placeholder="输入关键词搜索"
                          type="text"
                        ></FormKit>
                      </div>

                      <div class="mt-2">
                        <ul
                          class="box-border h-full w-full divide-y divide-gray-100"
                          role="list"
                        >
                          <li
                            v-for="(tag, index) in tags"
                            :key="index"
                            v-close-popper
                          >
                            <div
                              class="relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
                            >
                              <div class="relative flex flex-row items-center">
                                <div class="flex-1">
                                  <div class="flex flex-col sm:flex-row">
                                    <PostTag :tag="tag" />
                                  </div>
                                  <div class="mt-1 flex">
                                    <span class="text-xs text-gray-500">
                                      /tags/{{ tag.spec.slug }}
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
                                  </div>
                                </div>
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
                    <span class="mr-0.5">作者</span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <div class="h-96 w-80">
                      <div class="bg-white p-4">
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
                            class="cursor-pointer hover:bg-gray-50"
                          >
                            <div class="flex items-center space-x-4 px-4 py-3">
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

      <VEmpty
        v-if="!posts.items.length && !loading"
        message="你可以尝试刷新或者新建文章"
        title="当前没有文章"
      >
        <template #actions>
          <VSpace>
            <VButton @click="handleFetchPosts">刷新</VButton>
            <VButton :route="{ name: 'PostEditor' }" type="primary">
              <template #icon>
                <IconAddCircle class="h-full w-full" />
              </template>
              新建文章
            </VButton>
          </VSpace>
        </template>
      </VEmpty>
      <ul
        v-else
        class="box-border h-full w-full divide-y divide-gray-100"
        role="list"
      >
        <li v-for="(post, index) in posts.items" :key="index">
          <div
            :class="{
              'bg-gray-100': checkSelection(post.post),
            }"
            class="relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
          >
            <div
              v-show="checkSelection(post.post)"
              class="absolute inset-y-0 left-0 w-0.5 bg-primary"
            ></div>
            <div class="relative flex flex-row items-center">
              <div class="mr-4 hidden items-center sm:flex">
                <input
                  v-model="selectedPostNames"
                  :value="post.post.metadata.name"
                  class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                  name="post-checkbox"
                  type="checkbox"
                />
              </div>
              <div class="flex-1">
                <div class="flex flex-col sm:flex-row">
                  <RouterLink
                    :to="{
                      name: 'PostEditor',
                      query: { name: post.post.metadata.name },
                    }"
                  >
                    <span
                      class="mr-0 truncate text-sm font-medium text-gray-900 sm:mr-2"
                    >
                      {{ post.post.spec.title }}
                    </span>
                  </RouterLink>
                  <VSpace class="mt-1 sm:mt-0">
                    <RouterLink
                      v-for="(tag, tagIndex) in post.tags"
                      :key="tagIndex"
                      :to="{
                        name: 'Tags',
                        query: { name: tag.metadata.name },
                      }"
                    >
                      <PostTag :tag="tag"></PostTag>
                    </RouterLink>
                  </VSpace>
                </div>
                <div class="mt-1 flex">
                  <VSpace>
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
                    <span class="text-xs text-gray-500">访问量 0</span>
                    <span class="text-xs text-gray-500"> 评论 0 </span>
                  </VSpace>
                </div>
              </div>
              <div class="flex">
                <div
                  class="inline-flex flex-col items-end gap-4 sm:flex-row sm:items-center sm:gap-6"
                >
                  <RouterLink
                    v-for="(contributor, contributorIndex) in post.contributors"
                    :key="contributorIndex"
                    :to="{
                      name: 'UserDetail',
                      params: { name: contributor.name },
                    }"
                  >
                    <img
                      v-tooltip="contributor.displayName"
                      :alt="contributor.name"
                      :src="contributor.avatar"
                      :title="contributor.displayName"
                      class="hidden h-6 w-6 rounded-full ring-2 ring-white sm:inline-block"
                    />
                  </RouterLink>
                  <span class="text-sm text-gray-500">
                    {{ finalStatus(post.post) }}
                  </span>
                  <span>
                    <IconEye
                      v-if="post.post.spec.visible === 'PUBLIC'"
                      v-tooltip="`公开访问`"
                      class="cursor-pointer text-sm transition-all hover:text-blue-600"
                    />
                    <IconEyeOff
                      v-if="post.post.spec.visible === 'PRIVATE'"
                      v-tooltip="`私有访问`"
                      class="cursor-pointer text-sm transition-all hover:text-blue-600"
                    />
                    <IconTeam
                      v-if="post.post.spec.visible === 'INTERNAL'"
                      v-tooltip="`内部成员可访问`"
                      class="cursor-pointer text-sm transition-all hover:text-blue-600"
                    />
                  </span>
                  <time class="text-sm text-gray-500">
                    {{ formatDatetime(post.post.metadata.creationTimestamp) }}
                  </time>
                  <span>
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
                              @click="handleOpenSettingModal(post.post)"
                            >
                              设置
                            </VButton>
                            <VButton
                              v-close-popper
                              block
                              type="danger"
                              @click="handleDelete(post.post)"
                            >
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

      <template #footer>
        <div class="bg-white sm:flex sm:items-center sm:justify-end">
          <VPagination
            :page="posts.page"
            :size="posts.size"
            :total="posts.total"
            @change="handlePaginationChange"
          />
        </div>
      </template>
    </VCard>
  </div>
</template>
