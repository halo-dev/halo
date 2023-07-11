<script lang="ts" setup>
import {
  IconAddCircle,
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
  VDropdownDivider,
} from "@halo-dev/components";
import PostSettingModal from "./components/PostSettingModal.vue";
import PostTag from "../posts/tags/components/PostTag.vue";
import { computed, ref, watch } from "vue";
import type { Post, ListedPost } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { formatDatetime } from "@/utils/date";
import { usePermission } from "@/utils/permission";
import { postLabels } from "@/constants/labels";
import { useMutation, useQuery } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";
import { useRouteQuery } from "@vueuse/router";
import UserFilterDropdown from "@/components/filter/UserFilterDropdown.vue";
import CategoryFilterDropdown from "@/components/filter/CategoryFilterDropdown.vue";
import TagFilterDropdown from "@/components/filter/TagFilterDropdown.vue";

const { currentUserHasPermission } = usePermission();
const { t } = useI18n();

const settingModal = ref(false);
const selectedPost = ref<Post>();
const checkedAll = ref(false);
const selectedPostNames = ref<string[]>([]);

// Filters
const page = useRouteQuery<number>("page", 1, {
  transform: Number,
});
const size = useRouteQuery<number>("size", 20, {
  transform: Number,
});
const selectedVisible = useRouteQuery<
  "PUBLIC" | "INTERNAL" | "PRIVATE" | undefined
>("visible");
const selectedPublishStatus = useRouteQuery<string | undefined>("status");
const selectedSort = useRouteQuery<string | undefined>("sort");
const selectedCategory = useRouteQuery<string | undefined>("category");
const selectedTag = useRouteQuery<string | undefined>("tag");
const selectedContributor = useRouteQuery<string | undefined>("contributor");
const keyword = useRouteQuery<string>("keyword", "");
const total = ref(0);
const hasPrevious = ref(false);
const hasNext = ref(false);

watch(
  () => [
    selectedVisible.value,
    selectedPublishStatus.value,
    selectedSort.value,
    selectedCategory.value,
    selectedTag.value,
    selectedContributor.value,
    keyword.value,
  ],
  () => {
    page.value = 1;
  }
);

function handleClearFilters() {
  selectedVisible.value = undefined;
  selectedPublishStatus.value = undefined;
  selectedSort.value = undefined;
  selectedCategory.value = undefined;
  selectedTag.value = undefined;
  selectedContributor.value = undefined;
}

const hasFilters = computed(() => {
  return (
    selectedVisible.value ||
    selectedPublishStatus.value !== undefined ||
    selectedSort.value ||
    selectedCategory.value ||
    selectedTag.value ||
    selectedContributor.value
  );
});

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
    selectedPublishStatus,
    selectedVisible,
    selectedSort,
    keyword,
  ],
  queryFn: async () => {
    let categories: string[] | undefined;
    let tags: string[] | undefined;
    let contributors: string[] | undefined;
    const labelSelector: string[] = ["content.halo.run/deleted=false"];

    if (selectedCategory.value) {
      categories = [selectedCategory.value];
    }

    if (selectedTag.value) {
      tags = [selectedTag.value];
    }

    if (selectedContributor.value) {
      contributors = [selectedContributor.value];
    }

    if (selectedPublishStatus.value !== undefined) {
      labelSelector.push(
        `${postLabels.PUBLISHED}=${selectedPublishStatus.value}`
      );
    }

    const { data } = await apiClient.post.listPosts({
      labelSelector,
      page: page.value,
      size: size.value,
      visible: selectedVisible.value,
      sort: [selectedSort.value].filter(Boolean) as string[],
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

const getExternalUrl = (post: Post) => {
  if (post.metadata.labels?.[postLabels.PUBLISHED] === "true") {
    return post.status?.permalink;
  }
  return `/preview/posts/${post.metadata.name}`;
};
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
              <SearchInput v-if="!selectedPostNames.length" v-model="keyword" />
              <VSpace v-else>
                <VButton type="danger" @click="handleDeleteInBatch">
                  {{ $t("core.common.buttons.delete") }}
                </VButton>
              </VSpace>
            </div>
            <div class="mt-4 flex sm:mt-0">
              <VSpace spacing="lg">
                <FilterCleanButton
                  v-if="hasFilters"
                  @click="handleClearFilters"
                />
                <FilterDropdown
                  v-model="selectedPublishStatus"
                  :label="$t('core.common.filters.labels.status')"
                  :items="[
                    {
                      label: t('core.common.filters.item_labels.all'),
                      value: undefined,
                    },
                    {
                      label: t('core.post.filters.status.items.published'),
                      value: true,
                    },
                    {
                      label: t('core.post.filters.status.items.draft'),
                      value: false,
                    },
                  ]"
                />
                <FilterDropdown
                  v-model="selectedVisible"
                  :label="$t('core.post.filters.visible.label')"
                  :items="[
                    {
                      label: t('core.common.filters.item_labels.all'),
                      value: undefined,
                    },
                    {
                      label: t('core.post.filters.visible.items.public'),
                      value: 'PUBLIC',
                    },
                    {
                      label: t('core.post.filters.visible.items.private'),
                      value: 'PRIVATE',
                    },
                  ]"
                />
                <CategoryFilterDropdown
                  v-model="selectedCategory"
                  :label="$t('core.post.filters.category.label')"
                />
                <TagFilterDropdown
                  v-model="selectedTag"
                  :label="$t('core.post.filters.tag.label')"
                />
                <UserFilterDropdown
                  v-model="selectedContributor"
                  :label="$t('core.post.filters.author.label')"
                />
                <FilterDropdown
                  v-model="selectedSort"
                  :label="$t('core.common.filters.labels.sort')"
                  :items="[
                    {
                      label: t('core.common.filters.item_labels.default'),
                    },
                    {
                      label: t(
                        'core.post.filters.sort.items.publish_time_desc'
                      ),
                      value: 'publishTime,desc',
                    },
                    {
                      label: t('core.post.filters.sort.items.publish_time_asc'),
                      value: 'publishTime,asc',
                    },
                    {
                      label: t('core.post.filters.sort.items.create_time_desc'),
                      value: 'creationTimestamp,desc',
                    },
                    {
                      label: t('core.post.filters.sort.items.create_time_asc'),
                      value: 'creationTimestamp,asc',
                    },
                  ]"
                />
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
                        target="_blank"
                        :href="getExternalUrl(post.post)"
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
                <VDropdownItem
                  @click="
                    $router.push({
                      name: 'PostEditor',
                      query: { name: post.post.metadata.name },
                    })
                  "
                >
                  {{ $t("core.common.buttons.edit") }}
                </VDropdownItem>
                <VDropdownItem @click="handleOpenSettingModal(post.post)">
                  {{ $t("core.common.buttons.setting") }}
                </VDropdownItem>
                <VDropdownDivider />
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
