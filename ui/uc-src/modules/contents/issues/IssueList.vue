<script lang="ts" setup>
import {
  IconAddCircle,
  IconBookRead,
  IconRefreshLine,
  VButton,
  VCard,
  VEmpty,
  VEntityContainer,
  VLoading,
  VPageHeader,
  VPagination,
  VSpace,
  SearchInput,
  FilterCleanButton,
  FilterDropdown,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { useRouteQuery } from "@vueuse/router";
import { computed, watch } from "vue";
import IssueListItem from "./components/IssueListItem.vue";

const page = useRouteQuery<number>("page", 1, {
  transform: Number,
});
const size = useRouteQuery<number>("size", 20, {
  transform: Number,
});
const keyword = useRouteQuery<string>("keyword", "");

const selectedStatus = useRouteQuery<
  "OPEN" | "IN_PROGRESS" | "RESOLVED" | "CLOSED" | undefined
>("status");

function handleClearFilters() {
  selectedStatus.value = undefined;
}

const hasFilters = computed(() => {
  return selectedStatus.value !== undefined;
});

watch(
  () => [selectedStatus.value, keyword.value],
  () => {
    page.value = 1;
  }
);

const {
  data: issues,
  isLoading,
  isFetching,
  refetch,
} = useQuery({
  queryKey: ["my-issues", page, size, keyword, selectedStatus],
  queryFn: async () => {
    // For now, we'll use a mock API call until the real API client is generated
    // This would eventually be: ucApiClient.content.issue.listMyIssues({...})
    await new Promise(resolve => setTimeout(resolve, 1000)); // Simulate API delay
    
    // Mock response structure
    return {
      page: page.value,
      size: size.value,
      total: 0,
      items: [],
      first: true,
      last: true,
      hasNext: false,
      hasPrevious: false,
      totalPages: 0
    };
  },
  onSuccess(data) {
    page.value = data.page;
    size.value = data.size;
  },
  refetchInterval: false,
});
</script>

<template>
  <VPageHeader :title="$t('core.uc_issue.title')">
    <template #icon>
      <IconBookRead />
    </template>
    <template #actions>
      <VButton type="secondary">
        <template #icon>
          <IconAddCircle />
        </template>
        {{ $t("core.common.buttons.new") }}
      </VButton>
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <template #header>
        <div class="block w-full bg-gray-50 px-4 py-3">
          <div
            class="relative flex flex-col flex-wrap items-start justify-between gap-4 sm:flex-row sm:items-center"
          >
            <SearchInput v-model="keyword" />

            <VSpace spacing="lg" class="flex-wrap">
              <FilterCleanButton
                v-if="hasFilters"
                @click="handleClearFilters"
              />
              <FilterDropdown
                v-model="selectedStatus"
                :label="$t('core.common.filters.labels.status')"
                :items="[
                  {
                    label: $t('core.common.filters.item_labels.all'),
                    value: undefined,
                  },
                  {
                    label: $t('core.issue.filters.status.items.open'),
                    value: 'OPEN',
                  },
                  {
                    label: $t('core.issue.filters.status.items.in_progress'),
                    value: 'IN_PROGRESS',
                  },
                  {
                    label: $t('core.issue.filters.status.items.resolved'),
                    value: 'RESOLVED',
                  },
                  {
                    label: $t('core.issue.filters.status.items.closed'),
                    value: 'CLOSED',
                  },
                ]"
              />
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
            </VSpace>
          </div>
        </div>
      </template>
      <VLoading v-if="isLoading" />
      <Transition v-else-if="!issues?.items.length" appear name="fade">
        <VEmpty
          :message="$t('core.issue.empty.message')"
          :title="$t('core.issue.empty.title')"
        >
          <template #actions>
            <VSpace>
              <VButton @click="refetch">
                {{ $t("core.common.buttons.refresh") }}
              </VButton>
              <VButton type="secondary">
                <template #icon>
                  <IconAddCircle />
                </template>
                {{ $t("core.common.buttons.new") }}
              </VButton>
            </VSpace>
          </template>
        </VEmpty>
      </Transition>
      <Transition v-else appear name="fade">
        <VEntityContainer>
          <IssueListItem
            v-for="issue in issues.items"
            :key="issue.issue.metadata.name"
            :issue="issue"
          />
        </VEntityContainer>
      </Transition>

      <template #footer>
        <VPagination
          v-model:page="page"
          v-model:size="size"
          :page-label="$t('core.components.pagination.page_label')"
          :size-label="$t('core.components.pagination.size_label')"
          :total-label="
            $t('core.components.pagination.total_label', {
              total: issues?.total || 0,
            })
          "
          :total="issues?.total || 0"
          :size-options="[20, 30, 50, 100]"
        />
      </template>
    </VCard>
  </div>
</template>