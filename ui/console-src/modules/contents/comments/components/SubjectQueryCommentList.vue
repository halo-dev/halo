<script lang="ts" setup>
import FilterCleanButton from "@/components/filter/FilterCleanButton.vue";
import FilterDropdown from "@/components/filter/FilterDropdown.vue";
import UserFilterDropdown from "@/components/filter/UserFilterDropdown.vue";
import SearchInput from "@/components/input/SearchInput.vue";
import HasPermission from "@/components/permission/HasPermission.vue";
import {
  IconRefreshLine,
  VButton,
  VEmpty,
  VEntityContainer,
  VLoading,
  VPagination,
  VSpace,
} from "@halo-dev/components";
import { computed, ref, toRefs, watch } from "vue";
import useCommentsFetch from "../composables/use-comments-fetch";
import CommentListItem from "./CommentListItem.vue";

const props = defineProps<{
  subjectRefKey: string;
}>();

const { subjectRefKey } = toRefs(props);

const selectedApprovedStatus = ref();
const selectedSort = ref();
const selectedUser = ref();
const page = ref(1);
const size = ref(20);
const keyword = ref("");

const {
  data: comments,
  isLoading,
  isFetching,
  refetch,
} = useCommentsFetch(
  "core:comments:with-subject",
  page,
  size,
  selectedApprovedStatus,
  selectedSort,
  selectedUser,
  keyword,
  subjectRefKey
);

watch(
  () => [
    selectedApprovedStatus.value,
    selectedSort.value,
    selectedUser.value,
    keyword.value,
  ],
  () => {
    page.value = 1;
  }
);

const hasFilters = computed(() => {
  return (
    selectedApprovedStatus.value !== undefined ||
    selectedSort.value ||
    selectedUser.value
  );
});

function handleClearFilters() {
  selectedApprovedStatus.value = undefined;
  selectedSort.value = undefined;
  selectedUser.value = undefined;
}
</script>
<template>
  <div>
    <div class="mb-4 flex flex-wrap items-center justify-between gap-4">
      <SearchInput v-model="keyword" />
      <VSpace spacing="lg" class="flex-wrap">
        <FilterCleanButton v-if="hasFilters" @click="handleClearFilters" />
        <FilterDropdown
          v-model="selectedApprovedStatus"
          :label="$t('core.common.filters.labels.status')"
          :items="[
            {
              label: $t('core.common.filters.item_labels.all'),
            },
            {
              label: $t('core.comment.filters.status.items.approved'),
              value: true,
            },
            {
              label: $t('core.comment.filters.status.items.pending_review'),
              value: false,
            },
          ]"
        />
        <HasPermission :permissions="['system:users:view']">
          <UserFilterDropdown
            v-model="selectedUser"
            :label="$t('core.comment.filters.owner.label')"
          />
        </HasPermission>
        <FilterDropdown
          v-model="selectedSort"
          :label="$t('core.common.filters.labels.sort')"
          :items="[
            {
              label: $t('core.common.filters.item_labels.default'),
            },
            {
              label: $t('core.comment.filters.sort.items.last_reply_time_desc'),
              value: 'status.lastReplyTime,desc',
            },
            {
              label: $t('core.comment.filters.sort.items.last_reply_time_asc'),
              value: 'status.lastReplyTime,asc',
            },
            {
              label: $t('core.comment.filters.sort.items.reply_count_desc'),
              value: 'status.replyCount,desc',
            },
            {
              label: $t('core.comment.filters.sort.items.reply_count_asc'),
              value: 'status.replyCount,asc',
            },
            {
              label: $t('core.comment.filters.sort.items.create_time_desc'),
              value: 'metadata.creationTimestamp,desc',
            },
            {
              label: $t('core.comment.filters.sort.items.create_time_asc'),
              value: 'metadata.creationTimestamp,asc',
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
    <VLoading v-if="isLoading" />
    <Transition v-else-if="!comments?.items.length" appear name="fade">
      <VEmpty
        :message="$t('core.comment.empty.message')"
        :title="$t('core.comment.empty.title')"
      >
        <template #actions>
          <VButton @click="refetch">
            {{ $t("core.common.buttons.refresh") }}
          </VButton>
        </template>
      </VEmpty>
    </Transition>
    <Transition v-else appear name="fade">
      <div class="overflow-hidden rounded-base border">
        <VEntityContainer>
          <CommentListItem
            v-for="comment in comments.items"
            :key="comment.comment.metadata.name"
            :comment="comment"
          >
          </CommentListItem>
        </VEntityContainer>
      </div>
    </Transition>
    <div class="mt-4">
      <VPagination
        v-model:page="page"
        v-model:size="size"
        :page-label="$t('core.components.pagination.page_label')"
        :size-label="$t('core.components.pagination.size_label')"
        :total-label="
          $t('core.components.pagination.total_label', {
            total: comments?.total || 0,
          })
        "
        :total="comments?.total || 0"
        :size-options="[20, 30, 50, 100]"
      />
    </div>
  </div>
</template>
