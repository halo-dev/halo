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
  IconRefreshLine,
  IconExternalLinkLine,
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
import UserDropdownSelector from "@/components/dropdown-selector/UserDropdownSelector.vue";
import CategoryDropdownSelector from "@/components/dropdown-selector/CategoryDropdownSelector.vue";
import PostSettingModal from "./components/PostSettingModal.vue";
import PostTag from "../posts/tags/components/PostTag.vue";
import { computed, ref, watch } from "vue";
import type {
  User,
  Category,
  Post,
  Tag,
  ListedPost,
} from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { formatDatetime } from "@/utils/date";
import { usePermission } from "@/utils/permission";
import { postLabels } from "@/constants/labels";
import FilterTag from "@/components/filter/FilterTag.vue";
import FilteCleanButton from "@/components/filter/FilterCleanButton.vue";
import { getNode } from "@formkit/core";
import TagDropdownSelector from "@/components/dropdown-selector/TagDropdownSelector.vue";
import { useQuery } from "@tanstack/vue-query";

const { currentUserHasPermission } = usePermission();

const settingModal = ref(false);
const selectedPost = ref<Post>();
const checkedAll = ref(false);
const selectedPostNames = ref<string[]>([]);

// Filters
interface VisibleItem {
  label: string;
  value?: "PUBLIC" | "INTERNAL" | "PRIVATE";
}

interface PublishStatuItem {
  label: string;
  value?: boolean;
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
  // TODO: 支持内部成员可访问
  // {
  //   label: "内部成员可访问",
  //   value: "INTERNAL",
  // },
  {
    label: "私有",
    value: "PRIVATE",
  },
];

const PublishStatuItems: PublishStatuItem[] = [
  {
    label: "全部",
    value: undefined,
  },
  {
    label: "已发布",
    value: true,
  },
  {
    label: "未发布",
    value: false,
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

const selectedVisibleItem = ref<VisibleItem>(VisibleItems[0]);
const selectedPublishStatusItem = ref<PublishStatuItem>(PublishStatuItems[0]);
const selectedSortItem = ref<SortItem>();
const selectedCategory = ref<Category>();
const selectedTag = ref<Tag>();
const selectedContributor = ref<User>();
const keyword = ref("");

function handleVisibleItemChange(visibleItem: VisibleItem) {
  selectedVisibleItem.value = visibleItem;
  page.value = 1;
}

function handlePublishStatusItemChange(publishStatusItem: PublishStatuItem) {
  selectedPublishStatusItem.value = publishStatusItem;
  page.value = 1;
}

function handleSortItemChange(sortItem?: SortItem) {
  selectedSortItem.value = sortItem;
  page.value = 1;
}

function handleCategoryChange(category?: Category) {
  selectedCategory.value = category;
  page.value = 1;
}

function handleTagChange(tag?: Tag) {
  selectedTag.value = tag;
  page.value = 1;
}

function handleContributorChange(user?: User) {
  selectedContributor.value = user;
  page.value = 1;
}

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

function handleClearFilters() {
  selectedVisibleItem.value = VisibleItems[0];
  selectedPublishStatusItem.value = PublishStatuItems[0];
  selectedSortItem.value = undefined;
  selectedCategory.value = undefined;
  selectedTag.value = undefined;
  selectedContributor.value = undefined;
  keyword.value = "";
  page.value = 1;
}

const hasFilters = computed(() => {
  return (
    selectedVisibleItem.value.value ||
    selectedPublishStatusItem.value.value !== undefined ||
    selectedSortItem.value ||
    selectedCategory.value ||
    selectedTag.value ||
    selectedContributor.value ||
    keyword.value
  );
});

const page = ref(1);
const size = ref(20);
const total = ref(0);
const hasPrevious = ref(false);
const hasNext = ref(false);

const {
  data: posts,
  isLoading,
  isFetching,
  refetch,
} = useQuery<ListedPost[]>({
  queryKey: [
    "posts",
    page,
    size,
    selectedCategory,
    selectedTag,
    selectedContributor,
    selectedPublishStatusItem,
    selectedVisibleItem,
    selectedSortItem,
    keyword,
  ],
  queryFn: async () => {
    let categories: string[] | undefined;
    let tags: string[] | undefined;
    let contributors: string[] | undefined;
    const labelSelector: string[] = ["content.halo.run/deleted=false"];

    if (selectedCategory.value) {
      categories = [selectedCategory.value.metadata.name];
    }

    if (selectedTag.value) {
      tags = [selectedTag.value.metadata.name];
    }

    if (selectedContributor.value) {
      contributors = [selectedContributor.value.metadata.name];
    }

    if (selectedPublishStatusItem.value.value !== undefined) {
      labelSelector.push(
        `${postLabels.PUBLISHED}=${selectedPublishStatusItem.value.value}`
      );
    }

    const { data } = await apiClient.post.listPosts({
      labelSelector,
      page: page.value,
      size: size.value,
      visible: selectedVisibleItem.value?.value,
      sort: selectedSortItem.value?.sort,
      sortOrder: selectedSortItem.value?.sortOrder,
      keyword: keyword.value,
      category: categories,
      tag: tags,
      contributor: contributors,
    });

    total.value = data.total;
    hasNext.value = data.hasNext;
    hasPrevious.value = data.hasPrevious;

    return data.items;
  },
  refetchInterval: (data) => {
    const abnormalPosts = data?.filter((post) => {
      const { spec, metadata, status } = post.post;
      return (
        spec.deleted ||
        (spec.publish && metadata.labels?.[postLabels.PUBLISHED] !== "true") ||
        (spec.releaseSnapshot === spec.headSnapshot && status?.inProgress)
      );
    });

    return abnormalPosts?.length ? 3000 : false;
  },
  refetchOnWindowFocus: false,
});

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
  selectedPost.value = undefined;
  refetch();
};

const handleSelectPrevious = async () => {
  if (!posts.value) return;

  const index = posts.value.findIndex(
    (post) => post.post.metadata.name === selectedPost.value?.metadata.name
  );

  if (index > 0) {
    const { data: previousPost } =
      await apiClient.extension.post.getcontentHaloRunV1alpha1Post({
        name: posts.value[index - 1].post.metadata.name,
      });
    selectedPost.value = previousPost;
    return;
  }
  if (index === 0 && hasPrevious) {
    page.value--;
    await refetch();
    selectedPost.value = posts.value[posts.value.length - 1].post;
  }
};

const handleSelectNext = async () => {
  if (!posts.value) return;

  const index = posts.value.findIndex(
    (post) => post.post.metadata.name === selectedPost.value?.metadata.name
  );
  if (index < posts.value.length - 1) {
    const { data: nextPost } =
      await apiClient.extension.post.getcontentHaloRunV1alpha1Post({
        name: posts.value[index + 1].post.metadata.name,
      });
    selectedPost.value = nextPost;
    return;
  }
  if (index === posts.value.length - 1 && hasNext) {
    page.value++;
    await refetch();
    selectedPost.value = posts.value[0].post;
  }
};

const checkSelection = (post: Post) => {
  return (
    post.metadata.name === selectedPost.value?.metadata.name ||
    selectedPostNames.value.includes(post.metadata.name)
  );
};

const getPublishStatus = (post: Post) => {
  const { labels } = post.metadata;
  return labels?.[postLabels.PUBLISHED] === "true" ? "已发布" : "未发布";
};

const isPublishing = (post: Post) => {
  const { spec, status, metadata } = post;
  return (
    (spec.publish && metadata.labels?.[postLabels.PUBLISHED] !== "true") ||
    (spec.releaseSnapshot === spec.headSnapshot && status?.inProgress)
  );
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

const handleDelete = async (post: Post) => {
  Dialog.warning({
    title: "确定要删除该文章吗？",
    description: "该操作会将文章放入回收站，后续可以从回收站恢复",
    confirmType: "danger",
    onConfirm: async () => {
      await apiClient.post.recyclePost({
        name: post.metadata.name,
      });
      await refetch();

      Toast.success("删除成功");
    },
  });
};

const handleDeleteInBatch = async () => {
  Dialog.warning({
    title: "确定要删除选中的文章吗？",
    description: "该操作会将文章放入回收站，后续可以从回收站恢复",
    confirmType: "danger",
    onConfirm: async () => {
      await Promise.all(
        selectedPostNames.value.map((name) => {
          return apiClient.post.recyclePost({
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

watch(selectedPostNames, (newValue) => {
  checkedAll.value = newValue.length === posts.value?.length;
});
</script>
<template>
  <PostSettingModal
    v-model:visible="settingModal"
    :post="selectedPost"
    @close="onSettingModalClose"
  >
    <template #actions>
      <span @click="handleSelectPrevious">
        <IconArrowLeft v-tooltip="`上一项`" />
      </span>
      <span @click="handleSelectNext">
        <IconArrowRight v-tooltip="`下一项`" />
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
        <VButton :route="{ name: 'DeletedPosts' }" size="sm">回收站</VButton>

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

                <FilterTag
                  v-if="selectedPublishStatusItem.value !== undefined"
                  @close="handlePublishStatusItemChange(PublishStatuItems[0])"
                >
                  状态：{{ selectedPublishStatusItem.label }}
                </FilterTag>

                <FilterTag
                  v-if="selectedVisibleItem.value"
                  @close="handleVisibleItemChange(VisibleItems[0])"
                >
                  可见性：{{ selectedVisibleItem.label }}
                </FilterTag>

                <FilterTag
                  v-if="selectedCategory"
                  @close="handleCategoryChange()"
                >
                  分类：{{ selectedCategory.spec.displayName }}
                </FilterTag>

                <FilterTag v-if="selectedTag" @click="handleTagChange()">
                  标签：{{ selectedTag.spec.displayName }}
                </FilterTag>

                <FilterTag
                  v-if="selectedContributor"
                  @close="handleContributorChange()"
                >
                  作者：{{ selectedContributor.spec.displayName }}
                </FilterTag>

                <FilterTag
                  v-if="selectedSortItem"
                  @close="handleSortItemChange()"
                >
                  排序：{{ selectedSortItem.label }}
                </FilterTag>

                <FilteCleanButton
                  v-if="hasFilters"
                  @click="handleClearFilters"
                />
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
                          v-for="(filterItem, index) in PublishStatuItems"
                          :key="index"
                          v-close-popper
                          :class="{
                            'bg-gray-100':
                              selectedPublishStatusItem.value ===
                              filterItem.value,
                          }"
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                          @click="handlePublishStatusItemChange(filterItem)"
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
                <CategoryDropdownSelector
                  v-model:selected="selectedCategory"
                  @select="handleCategoryChange"
                >
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">分类</span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                </CategoryDropdownSelector>
                <TagDropdownSelector
                  v-model:selected="selectedTag"
                  @select="handleTagChange"
                >
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">标签</span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                </TagDropdownSelector>
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
                <div class="flex flex-row gap-2">
                  <div
                    class="group cursor-pointer rounded p-1 hover:bg-gray-200"
                    @click="refetch()"
                  >
                    <IconRefreshLine
                      v-tooltip="`刷新`"
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
        <VEmpty message="你可以尝试刷新或者新建文章" title="当前没有文章">
          <template #actions>
            <VSpace>
              <VButton @click="refetch">刷新</VButton>
              <VButton
                v-permission="['system:posts:manage']"
                :route="{ name: 'PostEditor' }"
                type="primary"
              >
                <template #icon>
                  <IconAddCircle class="h-full w-full" />
                </template>
                新建文章
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
                <VEntityField
                  :title="post.post.spec.title"
                  :route="{
                    name: 'PostEditor',
                    query: { name: post.post.metadata.name },
                  }"
                  width="27rem"
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
                      <a
                        v-if="post.post.status?.permalink"
                        target="_blank"
                        :href="post.post.status?.permalink"
                        :title="post.post.status?.permalink"
                        class="hidden text-gray-600 transition-all hover:text-gray-900 group-hover:inline-block"
                      >
                        <IconExternalLinkLine class="h-3.5 w-3.5" />
                      </a>
                    </VSpace>
                  </template>
                  <template #description>
                    <div class="flex flex-col gap-1.5">
                      <VSpace class="flex-wrap !gap-y-1">
                        <p
                          v-if="post.categories.length"
                          class="inline-flex flex-wrap gap-1 text-xs text-gray-500"
                        >
                          分类：<a
                            v-for="(category, categoryIndex) in post.categories"
                            :key="categoryIndex"
                            :href="category.status?.permalink"
                            :title="category.status?.permalink"
                            target="_blank"
                            class="cursor-pointer hover:text-gray-900"
                          >
                            {{ category.spec.displayName }}
                          </a>
                        </p>
                        <span class="text-xs text-gray-500">
                          访问量 {{ post.stats.visit || 0 }}
                        </span>
                        <span class="text-xs text-gray-500">
                          评论 {{ post.stats.totalComment || 0 }}
                        </span>
                        <span
                          v-if="post.post.spec.pinned"
                          class="text-xs text-gray-500"
                        >
                          已置顶
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
                <VEntityField :description="getPublishStatus(post.post)">
                  <template v-if="isPublishing(post.post)" #description>
                    <VStatusDot text="发布中" animate />
                  </template>
                </VEntityField>
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
                    <!-- TODO: 支持内部成员可访问 -->
                    <IconTeam
                      v-if="false"
                      v-tooltip="`内部成员可访问`"
                      class="cursor-pointer text-sm transition-all hover:text-blue-600"
                    />
                  </template>
                </VEntityField>
                <VEntityField v-if="post?.post?.spec.deleted">
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
