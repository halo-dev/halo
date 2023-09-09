<script lang="ts" setup>
import {
  IconAddCircle,
  IconArrowLeft,
  IconArrowRight,
  IconBookRead,
  IconRefreshLine,
  Dialog,
  VButton,
  VCard,
  VEmpty,
  VPageHeader,
  VPagination,
  VSpace,
  VLoading,
  Toast,
} from "@halo-dev/components";
import PostSettingModal from "./components/PostSettingModal.vue";
import { computed, ref, watch } from "vue";
import type { Post, ListedPost } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { postLabels } from "@/constants/labels";
import { useQuery } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";
import { useRouteQuery } from "@vueuse/router";
import UserFilterDropdown from "@/components/filter/UserFilterDropdown.vue";
import CategoryFilterDropdown from "@/components/filter/CategoryFilterDropdown.vue";
import TagFilterDropdown from "@/components/filter/TagFilterDropdown.vue";
import PostListItem from "./components/PostListItem.vue";
import { provide } from "vue";
import type { Ref } from "vue";

const { t } = useI18n();

const settingModal = ref(false);
const selectedPost = ref<Post>();
const checkedAll = ref(false);
const selectedPostNames = ref<string[]>([]);

provide<Ref<string[]>>("selectedPostNames", selectedPostNames);

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

    return abnormalPosts?.length ? 1000 : false;
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
                      value: 'true',
                    },
                    {
                      label: t('core.post.filters.status.items.draft'),
                      value: 'false',
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
          <li v-for="post in posts" :key="post.post.metadata.name">
            <PostListItem
              :post="post"
              :is-selected="checkSelection(post.post)"
              @open-setting-modal="handleOpenSettingModal"
            />
          </li>
        </ul>
      </Transition>

      <template #footer>
        <VPagination
          v-model:page="page"
          v-model:size="size"
          :page-label="$t('core.components.pagination.page_label')"
          :size-label="$t('core.components.pagination.size_label')"
          :total-label="
            $t('core.components.pagination.total_label', { total: total })
          "
          :total="total"
          :size-options="[20, 30, 50, 100]"
        />
      </template>
    </VCard>
  </div>
</template>
