<script lang="ts" setup>
import {
  IconAddCircle,
  IconArrowDown,
  IconArrowLeft,
  IconArrowRight,
  IconBookRead,
  IconEye,
  IconEyeOff,
  IconTeam,
  IconCloseCircle,
  useDialog,
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
} from "@halo-dev/components";
import UserDropdownSelector from "@/components/dropdown-selector/UserDropdownSelector.vue";
import PostSettingModal from "./components/PostSettingModal.vue";
import PostTag from "../posts/tags/components/PostTag.vue";
import { onMounted, ref, watch, watchEffect } from "vue";
import type {
  User,
  Category,
  ListedPostList,
  Post,
  PostRequest,
  Tag,
} from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { formatDatetime } from "@/utils/date";
import { usePostCategory } from "@/modules/contents/posts/categories/composables/use-post-category";
import { usePostTag } from "@/modules/contents/posts/tags/composables/use-post-tag";

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

const dialog = useDialog();

const handleFetchPosts = async () => {
  try {
    loading.value = true;

    let categories: string[] | undefined;
    let tags: string[] | undefined;
    let contributors: string[] | undefined;

    if (selectedCategory.value) {
      categories = [
        selectedCategory.value.metadata.name,
        selectedCategory.value.metadata.name,
      ];
    }

    if (selectedTag.value) {
      tags = [selectedTag.value.metadata.name];
    }

    if (selectedContributor.value) {
      contributors = [selectedContributor.value.metadata.name];
    }

    const { data } = await apiClient.post.listPosts({
      page: posts.value.page,
      size: posts.value.size,
      visible: selectedVisibleItem.value?.value,
      publishPhase: selectedPublishPhaseItem.value?.value,
      sort: selectedSortItem.value?.sort,
      sortOrder: selectedSortItem.value?.sortOrder,
      keyword: keyword.value,
      category: categories,
      tag: tags,
      contributor: contributors,
    });
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
    {
      name: post.metadata.name,
    }
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
      await apiClient.extension.post.getcontentHaloRunV1alpha1Post({
        name: items[index - 1].post.metadata.name,
      });
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
      await apiClient.extension.post.getcontentHaloRunV1alpha1Post({
        name: items[index + 1].post.metadata.name,
      });
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
      await apiClient.extension.post.deletecontentHaloRunV1alpha1Post({
        name: post.metadata.name,
      });
      await handleFetchPosts();
    },
  });
};

const handleDeleteInBatch = async () => {
  dialog.warning({
    title: "是否确认删除选中的文章？",
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
      selectedPostNames.value.length = 0;
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

  const { data: content } = await apiClient.content.obtainSnapshotContent({
    snapshotName: selectedPost.value.spec.headSnapshot,
  });

  selectedPostWithContent.value = {
    post: selectedPost.value,
    content: content,
  };
});

onMounted(() => {
  handleFetchPosts();
});

// Filters

interface VisibleItem {
  label: string;
  value?: "PUBLIC" | "INTERNAL" | "PRIVATE";
}

interface PublishPhaseItem {
  label: string;
  value?: "DRAFT" | "PENDING_APPROVAL" | "PUBLISHED";
}

interface SortItem {
  label: string;
  sort: "PUBLISH_TIME" | "CREATE_TIME";
  sortOrder: boolean;
}

const VisibleItems: VisibleItem[] = [
  {
    label: "全部",
    value: undefined,
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

const PublishPhaseItems: PublishPhaseItem[] = [
  {
    label: "全部",
    value: undefined,
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

const SortItems: SortItem[] = [
  {
    label: "较近发布",
    sort: "PUBLISH_TIME",
    sortOrder: false,
  },
  {
    label: "较早发布",
    sort: "PUBLISH_TIME",
    sortOrder: true,
  },
  {
    label: "较近创建",
    sort: "CREATE_TIME",
    sortOrder: false,
  },
  {
    label: "较早创建",
    sort: "CREATE_TIME",
    sortOrder: true,
  },
];

const { categories } = usePostCategory({ fetchOnMounted: true });
const { tags } = usePostTag({ fetchOnMounted: true });

const selectedVisibleItem = ref<VisibleItem>(VisibleItems[0]);
const selectedPublishPhaseItem = ref<PublishPhaseItem>(PublishPhaseItems[0]);
const selectedSortItem = ref<SortItem>();
const selectedCategory = ref<Category>();
const selectedTag = ref<Tag>();
const selectedContributor = ref<User>();
const keyword = ref("");

function handleVisibleItemChange(visibleItem: VisibleItem) {
  selectedVisibleItem.value = visibleItem;
  handleFetchPosts();
}

function handlePublishPhaseItemChange(publishPhaseItem: PublishPhaseItem) {
  selectedPublishPhaseItem.value = publishPhaseItem;
  handleFetchPosts();
}

function handleSortItemChange(sortItem?: SortItem) {
  selectedSortItem.value = sortItem;
  handleFetchPosts();
}

function handleCategoryChange(category?: Category) {
  selectedCategory.value = category;
  handleFetchPosts();
}

function handleTagChange(tag?: Tag) {
  selectedTag.value = tag;
  handleFetchPosts();
}

function handleContributorChange(user?: User) {
  selectedContributor.value = user;
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
      <span @click="handleSelectPrevious">
        <IconArrowLeft />
      </span>
      <span @click="handleSelectNext">
        <IconArrowRight />
      </span>
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
            <div class="flex w-full flex-1 items-center sm:w-auto">
              <div
                v-if="!selectedPostNames.length"
                class="flex items-center gap-2"
              >
                <FormKit
                  v-model="keyword"
                  placeholder="输入关键词搜索"
                  type="text"
                  @keyup.enter="handleFetchPosts"
                ></FormKit>

                <div
                  v-if="selectedPublishPhaseItem.value"
                  class="group flex cursor-pointer items-center justify-center gap-1 rounded-full bg-gray-200 px-2 py-1 hover:bg-gray-300"
                >
                  <span class="text-xs text-gray-600 group-hover:text-gray-900">
                    状态：{{ selectedPublishPhaseItem.label }}
                  </span>
                  <IconCloseCircle
                    class="h-4 w-4 text-gray-600"
                    @click="handlePublishPhaseItemChange(PublishPhaseItems[0])"
                  />
                </div>
                <div
                  v-if="selectedVisibleItem.value"
                  class="group flex cursor-pointer items-center justify-center gap-1 rounded-full bg-gray-200 px-2 py-1 hover:bg-gray-300"
                >
                  <span class="text-xs text-gray-600 group-hover:text-gray-900">
                    可见性：{{ selectedVisibleItem.label }}
                  </span>
                  <IconCloseCircle
                    class="h-4 w-4 text-gray-600"
                    @click="handleVisibleItemChange(VisibleItems[0])"
                  />
                </div>

                <div
                  v-if="selectedCategory"
                  class="group flex cursor-pointer items-center justify-center gap-1 rounded-full bg-gray-200 px-2 py-1 hover:bg-gray-300"
                >
                  <span class="text-xs text-gray-600 group-hover:text-gray-900">
                    分类：{{ selectedCategory.spec.displayName }}
                  </span>
                  <IconCloseCircle
                    class="h-4 w-4 text-gray-600"
                    @click="handleCategoryChange()"
                  />
                </div>
                <div
                  v-if="selectedTag"
                  class="group flex cursor-pointer items-center justify-center gap-1 rounded-full bg-gray-200 px-2 py-1 hover:bg-gray-300"
                >
                  <span class="text-xs text-gray-600 group-hover:text-gray-900">
                    标签：{{ selectedTag.spec.displayName }}
                  </span>
                  <IconCloseCircle
                    class="h-4 w-4 text-gray-600"
                    @click="handleTagChange()"
                  />
                </div>
                <div
                  v-if="selectedContributor"
                  class="group flex cursor-pointer items-center justify-center gap-1 rounded-full bg-gray-200 px-2 py-1 hover:bg-gray-300"
                >
                  <span class="text-xs text-gray-600 group-hover:text-gray-900">
                    作者：{{ selectedContributor.spec.displayName }}
                  </span>
                  <IconCloseCircle
                    class="h-4 w-4 text-gray-600"
                    @click="handleContributorChange()"
                  />
                </div>
                <div
                  v-if="selectedSortItem"
                  class="group flex cursor-pointer items-center justify-center gap-1 rounded-full bg-gray-200 px-2 py-1 hover:bg-gray-300"
                >
                  <span class="text-xs text-gray-600 group-hover:text-gray-900">
                    排序：{{ selectedSortItem.label }}
                  </span>
                  <IconCloseCircle
                    class="h-4 w-4 text-gray-600"
                    @click="handleSortItemChange()"
                  />
                </div>
              </div>
              <VSpace v-else>
                <VButton type="danger" @click="handleDeleteInBatch">
                  删除
                </VButton>
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
                          v-for="(filterItem, index) in PublishPhaseItems"
                          :key="index"
                          v-close-popper
                          :class="{
                            'bg-gray-100':
                              selectedPublishPhaseItem.value ===
                              filterItem.value,
                          }"
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                          @click="handlePublishPhaseItemChange(filterItem)"
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
                          v-for="(filterItem, index) in VisibleItems"
                          :key="index"
                          v-close-popper
                          :class="{
                            'bg-gray-100':
                              selectedVisibleItem.value === filterItem.value,
                          }"
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                          @click="handleVisibleItemChange(filterItem)"
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
                            @click="handleCategoryChange(category)"
                          >
                            <div
                              class="group relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
                              :class="{
                                'bg-gray-100':
                                  selectedCategory?.metadata.name ===
                                  category.metadata.name,
                              }"
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
                                      {{ category.status?.permalink }}
                                    </span>
                                  </div>
                                </div>
                                <div class="flex">
                                  <div
                                    class="inline-flex flex-col items-end gap-4 sm:flex-row sm:items-center sm:gap-6"
                                  >
                                    <div
                                      class="cursor-pointer text-sm text-gray-500 hover:text-gray-900"
                                    >
                                      {{ category.status?.postCount || 0 }}
                                      篇文章
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
                            @click="handleTagChange(tag)"
                          >
                            <div
                              class="relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
                              :class="{
                                'bg-gray-100':
                                  selectedTag?.metadata.name ===
                                  tag.metadata.name,
                              }"
                            >
                              <div class="relative flex flex-row items-center">
                                <div class="flex-1">
                                  <div class="flex flex-col sm:flex-row">
                                    <PostTag :tag="tag" />
                                  </div>
                                  <div class="mt-1 flex">
                                    <span class="text-xs text-gray-500">
                                      {{ tag.status?.permalink }}
                                    </span>
                                  </div>
                                </div>
                                <div class="flex">
                                  <div
                                    class="inline-flex flex-col items-end gap-4 sm:flex-row sm:items-center sm:gap-6"
                                  >
                                    <div
                                      class="cursor-pointer text-sm text-gray-500 hover:text-gray-900"
                                    >
                                      {{ tag.status?.postCount || 0 }}
                                      篇文章
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
                <UserDropdownSelector
                  v-model:selected="selectedContributor"
                  @select="handleContributorChange"
                >
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">作者</span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                </UserDropdownSelector>
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
                          v-for="(sortItem, index) in SortItems"
                          :key="index"
                          v-close-popper
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                          @click="handleSortItemChange(sortItem)"
                        >
                          <span class="truncate">{{ sortItem.label }}</span>
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
          <VEntity :is-selected="checkSelection(post.post)">
            <template #checkbox>
              <input
                v-model="selectedPostNames"
                :value="post.post.metadata.name"
                class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                name="post-checkbox"
                type="checkbox"
              />
            </template>
            <template #start>
              <VEntityField
                :title="post.post.spec.title"
                :route="{
                  name: 'PostEditor',
                  query: { name: post.post.metadata.name },
                }"
              >
                <template #extra>
                  <VSpace class="mt-1 sm:mt-0">
                    <RouterLink
                      v-if="post.post.status?.inProgress"
                      v-tooltip="`当前有内容已保存，但还未发布。`"
                      :to="{
                        name: 'PostEditor',
                        query: { name: post.post.metadata.name },
                      }"
                      class="flex items-center"
                    >
                      <VStatusDot state="success" animate />
                    </RouterLink>
                    <PostTag
                      v-for="(tag, tagIndex) in post.tags"
                      :key="tagIndex"
                      :tag="tag"
                      route
                    ></PostTag>
                  </VSpace>
                </template>
                <template #description>
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
                </template>
              </VEntityField>
            </template>
            <template #end>
              <VEntityField>
                <template #description>
                  <RouterLink
                    v-for="(contributor, contributorIndex) in post.contributors"
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
              <VEntityField
                :description="finalStatus(post.post)"
              ></VEntityField>
              <VEntityField>
                <template #description>
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
                    {{ formatDatetime(post.post.metadata.creationTimestamp) }}
                  </span>
                </template>
              </VEntityField>
            </template>
            <template #dropdownItems>
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
            </template>
          </VEntity>
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
