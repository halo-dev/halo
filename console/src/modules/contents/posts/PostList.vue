<script lang="ts" setup>
import {
  IconAddCircle,
  IconArrowDown,
  IconArrowLeft,
  IconArrowRight,
  IconBookRead,
  IconEye,
  IconEyeOff,
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
  VDropdownItem,
  VDropdown,
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
import FilterCleanButton from "@/components/filter/FilterCleanButton.vue";
import { getNode } from "@formkit/core";
import TagDropdownSelector from "@/components/dropdown-selector/TagDropdownSelector.vue";
import { useMutation, useQuery } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";

const { currentUserHasPermission } = usePermission();
const { t } = useI18n();

const settingModal = ref(false);
const selectedPost = ref<Post>();
const checkedAll = ref(false);
const selectedPostNames = ref<string[]>([]);

// Filters
interface VisibleItem {
  label: string;
  value?: "PUBLIC" | "INTERNAL" | "PRIVATE";
}

interface PublishStatusItem {
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
    label: t("core.post.filters.visible.items.all"),
    value: undefined,
  },
  {
    label: t("core.post.filters.visible.items.public"),
    value: "PUBLIC",
  },
  {
    label: t("core.post.filters.visible.items.private"),
    value: "PRIVATE",
  },
];

const PublishStatusItems: PublishStatusItem[] = [
  {
    label: t("core.post.filters.status.items.all"),
    value: undefined,
  },
  {
    label: t("core.post.filters.status.items.published"),
    value: true,
  },
  {
    label: t("core.post.filters.status.items.draft"),
    value: false,
  },
];

const SortItems: SortItem[] = [
  {
    label: t("core.post.filters.sort.items.publish_time_desc"),
    sort: "PUBLISH_TIME",
    sortOrder: false,
  },
  {
    label: t("core.post.filters.sort.items.publish_time_asc"),
    sort: "PUBLISH_TIME",
    sortOrder: true,
  },
  {
    label: t("core.post.filters.sort.items.create_time_desc"),
    sort: "CREATE_TIME",
    sortOrder: false,
  },
  {
    label: t("core.post.filters.sort.items.create_time_asc"),
    sort: "CREATE_TIME",
    sortOrder: true,
  },
];

const selectedVisibleItem = ref<VisibleItem>(VisibleItems[0]);
const selectedPublishStatusItem = ref<PublishStatusItem>(PublishStatusItems[0]);
const selectedSortItem = ref<SortItem>();
const selectedCategory = ref<Category>();
const selectedTag = ref<Tag>();
const selectedContributor = ref<User>();
const keyword = ref("");

function handleVisibleItemChange(visibleItem: VisibleItem) {
  selectedVisibleItem.value = visibleItem;
  page.value = 1;
}

function handlePublishStatusItemChange(publishStatusItem: PublishStatusItem) {
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
  selectedPublishStatusItem.value = PublishStatusItems[0];
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
  return labels?.[postLabels.PUBLISHED] === "true"
    ? t("core.post.filters.status.items.published")
    : t("core.post.filters.status.items.draft");
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
    title: t("core.post.operations.delete.title"),
    description: t("core.post.operations.delete.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      await apiClient.post.recyclePost({
        name: post.metadata.name,
      });
      await refetch();

      Toast.success(t("core.common.toast.delete_success"));
    },
  });
};

const handleDeleteInBatch = async () => {
  Dialog.warning({
    title: t("core.post.operations.delete_in_batch.title"),
    description: t("core.post.operations.delete_in_batch.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
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

      Toast.success(t("core.common.toast.delete_success"));
    },
  });
};

watch(selectedPostNames, (newValue) => {
  checkedAll.value = newValue.length === posts.value?.length;
});

const { mutate: changeVisibleMutation } = useMutation({
  mutationFn: async (post: Post) => {
    const { data } =
      await apiClient.extension.post.getcontentHaloRunV1alpha1Post({
        name: post.metadata.name,
      });
    data.spec.visible = data.spec.visible === "PRIVATE" ? "PUBLIC" : "PRIVATE";
    await apiClient.extension.post.updatecontentHaloRunV1alpha1Post(
      {
        name: post.metadata.name,
        post: data,
      },
      {
        mute: true,
      }
    );
    await refetch();
  },
  retry: 3,
  onSuccess: () => {
    Toast.success(t("core.common.toast.operation_success"));
  },
  onError: () => {
    Toast.error(t("core.common.toast.operation_failed"));
  },
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
        <IconArrowLeft v-tooltip="$t('core.common.buttons.previous')" />
      </span>
      <span @click="handleSelectNext">
        <IconArrowRight v-tooltip="$t('core.common.buttons.next')" />
      </span>
    </template>
  </PostSettingModal>
  <VPageHeader :title="$t('core.post.title')">
    <template #icon>
      <IconBookRead class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <VButton :route="{ name: 'Categories' }" size="sm">
          {{ $t("core.post.actions.categories") }}
        </VButton>
        <VButton :route="{ name: 'Tags' }" size="sm">
          {{ $t("core.post.actions.tags") }}
        </VButton>
        <VButton :route="{ name: 'DeletedPosts' }" size="sm">
          {{ $t("core.post.actions.recycle_bin") }}
        </VButton>

        <VButton
          v-permission="['system:posts:manage']"
          :route="{ name: 'PostEditor' }"
          type="secondary"
        >
          <template #icon>
            <IconAddCircle class="h-full w-full" />
          </template>
          {{ $t("core.common.buttons.new") }}
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
                  :placeholder="$t('core.common.placeholder.search')"
                  type="text"
                  name="keyword"
                  :model-value="keyword"
                  @keyup.enter="handleKeywordChange"
                ></FormKit>

                <FilterTag v-if="keyword" @close="handleClearKeyword()">
                  {{
                    $t("core.common.filters.results.keyword", {
                      keyword: keyword,
                    })
                  }}
                </FilterTag>

                <FilterTag
                  v-if="selectedPublishStatusItem.value !== undefined"
                  @close="handlePublishStatusItemChange(PublishStatusItems[0])"
                >
                  {{
                    $t("core.common.filters.results.status", {
                      status: selectedPublishStatusItem.label,
                    })
                  }}
                </FilterTag>

                <FilterTag
                  v-if="selectedVisibleItem.value"
                  @close="handleVisibleItemChange(VisibleItems[0])"
                >
                  {{
                    $t("core.post.filters.visible.result", {
                      visible: selectedVisibleItem.label,
                    })
                  }}
                </FilterTag>

                <FilterTag
                  v-if="selectedCategory"
                  @close="handleCategoryChange()"
                >
                  {{
                    $t("core.post.filters.category.result", {
                      category: selectedCategory.spec.displayName,
                    })
                  }}
                </FilterTag>

                <FilterTag v-if="selectedTag" @click="handleTagChange()">
                  {{
                    $t("core.post.filters.tag.result", {
                      tag: selectedTag.spec.displayName,
                    })
                  }}
                </FilterTag>

                <FilterTag
                  v-if="selectedContributor"
                  @close="handleContributorChange()"
                >
                  {{
                    $t("core.post.filters.author.result", {
                      author: selectedContributor.spec.displayName,
                    })
                  }}
                </FilterTag>

                <FilterTag
                  v-if="selectedSortItem"
                  @close="handleSortItemChange()"
                >
                  {{
                    $t("core.common.filters.results.sort", {
                      sort: selectedSortItem.label,
                    })
                  }}
                </FilterTag>

                <FilterCleanButton
                  v-if="hasFilters"
                  @click="handleClearFilters"
                />
              </div>
              <VSpace v-else>
                <VButton type="danger" @click="handleDeleteInBatch">
                  {{ $t("core.common.buttons.delete") }}
                </VButton>
              </VSpace>
            </div>
            <div class="mt-4 flex sm:mt-0">
              <VSpace spacing="lg">
                <VDropdown>
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">
                      {{ $t("core.common.filters.labels.status") }}
                    </span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <VDropdownItem
                      v-for="(filterItem, index) in PublishStatusItems"
                      :key="index"
                      :selected="
                        filterItem.value === selectedPublishStatusItem.value
                      "
                      @click="handlePublishStatusItemChange(filterItem)"
                    >
                      {{ filterItem.label }}
                    </VDropdownItem>
                  </template>
                </VDropdown>
                <VDropdown>
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">
                      {{ $t("core.post.filters.visible.label") }}
                    </span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <VDropdownItem
                      v-for="(filterItem, index) in VisibleItems"
                      :key="index"
                      :selected="filterItem.value === selectedVisibleItem.value"
                      @click="handleVisibleItemChange(filterItem)"
                    >
                      {{ filterItem.label }}
                    </VDropdownItem>
                  </template>
                </VDropdown>
                <CategoryDropdownSelector
                  v-model:selected="selectedCategory"
                  @select="handleCategoryChange"
                >
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">
                      {{ $t("core.post.filters.category.label") }}
                    </span>
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
                    <span class="mr-0.5">
                      {{ $t("core.post.filters.tag.label") }}
                    </span>
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
                    <span class="mr-0.5">
                      {{ $t("core.post.filters.author.label") }}
                    </span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                </UserDropdownSelector>
                <VDropdown>
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">
                      {{ $t("core.common.filters.labels.sort") }}
                    </span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <VDropdownItem
                      v-for="(sortItem, index) in SortItems"
                      :key="index"
                      :selected="
                        sortItem.sort === selectedSortItem?.sort &&
                        sortItem.sortOrder === selectedSortItem?.sortOrder
                      "
                      @click="handleSortItemChange(sortItem)"
                    >
                      {{ sortItem.label }}
                    </VDropdownItem>
                  </template>
                </VDropdown>
                <div class="flex flex-row gap-2">
                  <div
                    class="group cursor-pointer rounded p-1 hover:bg-gray-200"
                    @click="refetch()"
                  >
                    <IconRefreshLine
                      v-tooltip="$t('core.common.buttons.refresh')"
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
          :message="$t('core.post.empty.message')"
          :title="$t('core.post.empty.title')"
        >
          <template #actions>
            <VSpace>
              <VButton @click="refetch">
                {{ $t("core.common.buttons.refresh") }}
              </VButton>
              <VButton
                v-permission="['system:posts:manage']"
                :route="{ name: 'PostEditor' }"
                type="primary"
              >
                <template #icon>
                  <IconAddCircle class="h-full w-full" />
                </template>
                {{ $t("core.common.buttons.new") }}
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
                        v-tooltip="
                          $t('core.common.tooltips.unpublished_content_tip')
                        "
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
                          {{ $t("core.post.list.fields.categories") }}
                          <a
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
                          {{
                            $t("core.post.list.fields.visits", {
                              visits: post.stats.visit,
                            })
                          }}
                        </span>
                        <span class="text-xs text-gray-500">
                          {{
                            $t("core.post.list.fields.comments", {
                              comments: post.stats.totalComment || 0,
                            })
                          }}
                        </span>
                        <span
                          v-if="post.post.spec.pinned"
                          class="text-xs text-gray-500"
                        >
                          {{ $t("core.post.list.fields.pinned") }}
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
                    <VStatusDot
                      :text="$t('core.common.tooltips.publishing')"
                      animate
                    />
                  </template>
                </VEntityField>
                <VEntityField>
                  <template #description>
                    <IconEye
                      v-if="post.post.spec.visible === 'PUBLIC'"
                      v-tooltip="$t('core.post.filters.visible.items.public')"
                      class="cursor-pointer text-sm transition-all hover:text-blue-600"
                      @click="changeVisibleMutation(post.post)"
                    />
                    <IconEyeOff
                      v-if="post.post.spec.visible === 'PRIVATE'"
                      v-tooltip="$t('core.post.filters.visible.items.private')"
                      class="cursor-pointer text-sm transition-all hover:text-blue-600"
                      @click="changeVisibleMutation(post.post)"
                    />
                  </template>
                </VEntityField>
                <VEntityField v-if="post?.post?.spec.deleted">
                  <template #description>
                    <VStatusDot
                      v-tooltip="$t('core.common.status.deleting')"
                      state="warning"
                      animate
                    />
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
                <VDropdownItem @click="handleOpenSettingModal(post.post)">
                  {{ $t("core.common.buttons.setting") }}
                </VDropdownItem>
                <VDropdownItem type="danger" @click="handleDelete(post.post)">
                  {{ $t("core.common.buttons.delete") }}
                </VDropdownItem>
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
            :page-label="$t('core.components.pagination.page_label')"
            :size-label="$t('core.components.pagination.size_label')"
            :total="total"
            :size-options="[20, 30, 50, 100]"
          />
        </div>
      </template>
    </VCard>
  </div>
</template>
