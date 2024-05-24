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
  VStatusDot,
  VEntity,
  VEntityField,
  VLoading,
  Toast,
  VDropdownItem,
} from "@halo-dev/components";
import PostTag from "./tags/components/PostTag.vue";
import { ref, watch } from "vue";
import type { ListedPost, Post } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { formatDatetime } from "@/utils/date";
import { usePermission } from "@/utils/permission";
import { cloneDeep } from "lodash-es";
import { useQuery } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";
import ContributorList from "../_components/ContributorList.vue";

const { currentUserHasPermission } = usePermission();
const { t } = useI18n();

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
  refetchInterval: (data) => {
    const deletingPosts = data?.some(
      (post) =>
        !!post.post.metadata.deletionTimestamp || !post.post.spec.deleted
    );
    return deletingPosts ? 1000 : false;
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
    title: t("core.deleted_post.operations.delete.title"),
    description: t("core.deleted_post.operations.delete.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      await apiClient.extension.post.deleteContentHaloRunV1alpha1Post({
        name: post.metadata.name,
      });
      await refetch();

      Toast.success(t("core.common.toast.delete_success"));
    },
  });
};

const handleDeletePermanentlyInBatch = async () => {
  Dialog.warning({
    title: t("core.deleted_post.operations.delete_in_batch.title"),
    description: t("core.deleted_post.operations.delete_in_batch.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      await Promise.all(
        selectedPostNames.value.map((name) => {
          return apiClient.extension.post.deleteContentHaloRunV1alpha1Post({
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

const handleRecovery = async (post: Post) => {
  Dialog.warning({
    title: t("core.deleted_post.operations.recovery.title"),
    description: t("core.deleted_post.operations.recovery.description"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      const postToUpdate = cloneDeep(post);
      postToUpdate.spec.deleted = false;
      await apiClient.extension.post.updateContentHaloRunV1alpha1Post({
        name: postToUpdate.metadata.name,
        post: postToUpdate,
      });

      await refetch();

      Toast.success(t("core.common.toast.recovery_success"));
    },
  });
};

const handleRecoveryInBatch = async () => {
  Dialog.warning({
    title: t("core.deleted_post.operations.recovery_in_batch.title"),
    description: t(
      "core.deleted_post.operations.recovery_in_batch.description"
    ),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      await Promise.all(
        selectedPostNames.value.map((name) => {
          const post = posts.value?.find(
            (item) => item.post.metadata.name === name
          )?.post;

          if (!post) {
            return Promise.resolve();
          }

          return apiClient.extension.post.updateContentHaloRunV1alpha1Post({
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

      Toast.success(t("core.common.toast.recovery_success"));
    },
  });
};

watch(selectedPostNames, (newValue) => {
  checkedAll.value = newValue.length === posts.value?.length;
});

watch(
  () => keyword.value,
  () => {
    page.value = 1;
  }
);
</script>
<template>
  <VPageHeader :title="$t('core.deleted_post.title')">
    <template #icon>
      <IconDeleteBin class="mr-2 self-center text-green-600" />
    </template>
    <template #actions>
      <VSpace>
        <VButton :route="{ name: 'Posts' }" size="sm">
          {{ $t("core.common.buttons.back") }}
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
            class="relative flex flex-col flex-wrap items-start gap-4 sm:flex-row sm:items-center"
          >
            <div
              v-permission="['system:posts:manage']"
              class="hidden items-center sm:flex"
            >
              <input
                v-model="checkedAll"
                type="checkbox"
                @change="handleCheckAllChange"
              />
            </div>
            <div class="flex w-full flex-1 items-center sm:w-auto">
              <SearchInput v-if="!selectedPostNames.length" v-model="keyword" />
              <VSpace v-else>
                <VButton type="danger" @click="handleDeletePermanentlyInBatch">
                  {{ $t("core.common.buttons.delete_permanently") }}
                </VButton>
                <VButton type="default" @click="handleRecoveryInBatch">
                  {{ $t("core.common.buttons.recovery") }}
                </VButton>
              </VSpace>
            </div>
            <VSpace spacing="lg" class="flex-wrap">
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
      </template>

      <VLoading v-if="isLoading" />

      <Transition v-else-if="!posts?.length" appear name="fade">
        <VEmpty
          :message="$t('core.deleted_post.empty.message')"
          :title="$t('core.deleted_post.empty.title')"
        >
          <template #actions>
            <VSpace>
              <VButton @click="refetch">
                {{ $t("core.common.buttons.refresh") }}
              </VButton>
              <VButton :route="{ name: 'Posts' }" type="primary">
                {{ $t("core.common.buttons.back") }}
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
                          {{ $t("core.post.list.fields.categories") }}
                          <span
                            v-for="(category, categoryIndex) in post.categories"
                            :key="categoryIndex"
                            class="cursor-pointer hover:text-gray-900"
                          >
                            {{ category.spec.displayName }}
                          </span>
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
                    <ContributorList :contributors="post.contributors" />
                  </template>
                </VEntityField>
                <VEntityField v-if="!post?.post?.spec.deleted">
                  <template #description>
                    <VStatusDot
                      v-tooltip="$t('core.common.tooltips.recovering')"
                      state="success"
                      animate
                    />
                  </template>
                </VEntityField>
                <VEntityField v-if="post?.post?.metadata.deletionTimestamp">
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
                  type="danger"
                  @click="handleDeletePermanently(post.post)"
                >
                  {{ $t("core.common.buttons.delete_permanently") }}
                </VDropdownItem>
                <VDropdownItem @click="handleRecovery(post.post)">
                  {{ $t("core.common.buttons.recovery") }}
                </VDropdownItem>
              </template>
            </VEntity>
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
