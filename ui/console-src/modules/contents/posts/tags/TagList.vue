<script lang="ts" setup>
import type { Tag } from "@halo-dev/api-client";
import { onMounted, ref, watch } from "vue";
import {
  IconAddCircle,
  IconBookRead,
  VButton,
  VCard,
  VEmpty,
  VPageHeader,
  VSpace,
  VLoading,
  VPagination,
  IconRefreshLine,
} from "@halo-dev/components";
import HasPermission from "@/components/permission/HasPermission.vue";
import TagEditingModal from "./components/TagEditingModal.vue";
import { useRouteQuery } from "@vueuse/router";
import { apiClient } from "@/utils/api-client";
import { usePostTag } from "./composables/use-post-tag";
import TagListItem from "./components/TagListItem.vue";
import SearchInput from "@/components/input/SearchInput.vue";
import FilterCleanButton from "@/components/filter/FilterCleanButton.vue";
import { computed } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const editingModal = ref(false);
const selectedTag = ref<Tag>();

const selectedTagNames = ref<string[]>([]);
const checkedAll = ref(false);

const keyword = useRouteQuery<string>("keyword", "");
const page = useRouteQuery<number>("page", 1, {
  transform: Number,
});
const size = useRouteQuery<number>("size", 20, {
  transform: Number,
});
const selectedSort = useRouteQuery<string | undefined>("sort");

const hasFilters = computed(() => {
  return !!selectedSort.value;
});

const handleClearFilters = () => {
  selectedSort.value = undefined;
};

const {
  tags,
  total,
  hasNext,
  hasPrevious,
  isLoading,
  isFetching,
  handleFetchTags,
  handleDelete,
  handleDeleteInBatch,
} = usePostTag({
  page,
  size,
  keyword,
  sort: selectedSort,
});

const handleOpenEditingModal = (tag?: Tag) => {
  selectedTag.value = tag;
  editingModal.value = true;
};

const handleDeleteTagInBatch = () => {
  handleDeleteInBatch(selectedTagNames.value).then(() => {
    selectedTagNames.value = [];
  });
};

const handleCheckAllChange = () => {
  if (checkedAll.value) {
    selectedTagNames.value = tags.value?.map((tag) => tag.metadata.name) || [];
  } else {
    selectedTagNames.value = [];
  }
};

const handleSelectPrevious = async () => {
  if (!tags.value) return;

  const currentIndex = tags.value.findIndex(
    (tag) => tag.metadata.name === selectedTag.value?.metadata.name
  );

  if (currentIndex > 0) {
    selectedTag.value = tags.value[currentIndex - 1];
    return;
  }

  if (currentIndex === 0 && hasPrevious.value) {
    page.value--;
    await handleFetchTags();
    setTimeout(() => {
      if (!tags.value) return;
      selectedTag.value = tags.value[tags.value.length - 1];
    });
  }
};

const handleSelectNext = async () => {
  if (!tags.value) return;

  if (!selectedTag.value) {
    selectedTag.value = tags.value[0];
    return;
  }
  const currentIndex = tags.value.findIndex(
    (tag) => tag.metadata.name === selectedTag.value?.metadata.name
  );
  if (currentIndex !== tags.value.length - 1) {
    selectedTag.value = tags.value[currentIndex + 1];
  }

  if (currentIndex === tags.value.length - 1 && hasNext.value) {
    page.value++;
    await handleFetchTags();
    setTimeout(() => {
      if (!tags.value) return;
      selectedTag.value = tags.value[0];
    });
  }
};

const onEditingModalClose = () => {
  selectedTag.value = undefined;
  queryName.value = null;
  editingModal.value = false;
  handleFetchTags();
};

const queryName = useRouteQuery("name");

onMounted(async () => {
  if (queryName.value) {
    const { data } = await apiClient.extension.tag.getContentHaloRunV1alpha1Tag(
      {
        name: queryName.value as string,
      }
    );
    selectedTag.value = data;
    editingModal.value = true;
  }
});

watch(selectedTagNames, (newVal) => {
  checkedAll.value = newVal.length === tags.value?.length;
});
</script>
<template>
  <TagEditingModal
    v-if="editingModal"
    :tag="selectedTag"
    @close="onEditingModalClose"
    @next="handleSelectNext"
    @previous="handleSelectPrevious"
  />
  <VPageHeader :title="$t('core.post_tag.title')">
    <template #icon>
      <IconBookRead class="mr-2 self-center" />
    </template>
    <template #actions>
      <VButton
        v-permission="['system:posts:manage']"
        type="secondary"
        @click="editingModal = true"
      >
        <template #icon>
          <IconAddCircle class="h-full w-full" />
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
            class="relative flex h-9 flex-col flex-wrap items-start gap-4 sm:flex-row sm:items-center"
          >
            <HasPermission :permissions="['system:posts:manage']">
              <div class="hidden items-center sm:flex">
                <input
                  v-model="checkedAll"
                  type="checkbox"
                  @change="handleCheckAllChange"
                />
              </div>
            </HasPermission>
            <div class="flex w-full flex-1 items-center sm:w-auto">
              <VSpace v-if="selectedTagNames.length > 0">
                <VButton type="danger" @click="handleDeleteTagInBatch">
                  {{ $t("core.common.buttons.delete") }}
                </VButton>
              </VSpace>
              <SearchInput v-else v-model="keyword" />
            </div>
            <VSpace spacing="lg" class="flex-wrap">
              <FilterCleanButton
                v-if="hasFilters"
                @click="handleClearFilters"
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
                      'core.post.tag.filters.sort.items.create_time_desc'
                    ),
                    value: 'metadata.creationTimestamp,desc',
                  },
                  {
                    label: t(
                      'core.post.tag.filters.sort.items.create_time_asc'
                    ),
                    value: 'metadata.creationTimestamp,asc',
                  },
                  {
                    label: t(
                      'core.post.tag.filters.sort.items.display_name_desc'
                    ),
                    value: 'spec.displayName,desc',
                  },
                  {
                    label: t(
                      'core.post.tag.filters.sort.items.display_name_asc'
                    ),
                    value: 'spec.displayName,asc',
                  },
                ]"
              />
              <div class="flex flex-row gap-2">
                <div
                  class="group cursor-pointer rounded p-1 hover:bg-gray-200"
                  @click="handleFetchTags()"
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
      </template>
      <VLoading v-if="isLoading" />
      <Transition v-else-if="!tags?.length" appear name="fade">
        <VEmpty
          :message="$t('core.post_tag.empty.message')"
          :title="$t('core.post_tag.empty.title')"
        >
          <template #actions>
            <VSpace>
              <VButton @click="() => handleFetchTags">
                {{ $t("core.common.buttons.refresh") }}
              </VButton>
              <VButton type="primary" @click="editingModal = true">
                <template #icon>
                  <IconAddCircle class="h-full w-full" />
                </template>
                {{ $t("core.common.buttons.new") }}
              </VButton>
            </VSpace>
          </template>
        </VEmpty>
      </Transition>

      <Transition appear name="fade">
        <ul
          class="box-border h-full w-full divide-y divide-gray-100"
          role="list"
        >
          <li v-for="(tag, index) in tags" :key="index">
            <TagListItem
              :tag="tag"
              :is-selected="selectedTag?.metadata.name === tag.metadata.name"
              @editing="handleOpenEditingModal"
              @delete="handleDelete"
            >
              <template #checkbox>
                <input
                  v-model="selectedTagNames"
                  :value="tag.metadata.name"
                  type="checkbox"
                />
              </template>
            </TagListItem>
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
