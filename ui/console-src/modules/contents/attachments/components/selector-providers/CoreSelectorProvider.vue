<script lang="ts" setup>
import LazyImage from "@/components/image/LazyImage.vue";
import { isImage } from "@/utils/image";
import { matchMediaTypes } from "@/utils/media-type";
import type { Attachment, Group } from "@halo-dev/api-client";
import {
  IconArrowLeft,
  IconArrowRight,
  IconCheckboxCircle,
  IconCheckboxFill,
  IconEye,
  IconGrid,
  IconList,
  IconRefreshLine,
  IconUpload,
  VButton,
  VCard,
  VEmpty,
  VLoading,
  VPagination,
  VSpace,
} from "@halo-dev/components";
import type { AttachmentLike } from "@halo-dev/console-shared";
import { useLocalStorage } from "@vueuse/core";
import { computed, ref, watch, watchEffect } from "vue";
import { useI18n } from "vue-i18n";
import { useAttachmentControl } from "../../composables/use-attachment";
import { useFetchAttachmentPolicy } from "../../composables/use-attachment-policy";
import AttachmentDetailModal from "../AttachmentDetailModal.vue";
import AttachmentGroupList from "../AttachmentGroupList.vue";
import AttachmentUploadModal from "../AttachmentUploadModal.vue";
import AttachmentSelectorListItem from "./components/AttachmentSelectorListItem.vue";

const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    selected: AttachmentLike[];
    accepts?: string[];
    min?: number;
    max?: number;
  }>(),
  {
    selected: () => [],
    accepts: () => ["*/*"],
    min: undefined,
    max: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:selected", attachments: AttachmentLike[]): void;
  (event: "change-provider", providerId: string): void;
}>();

const { policies } = useFetchAttachmentPolicy();

const keyword = ref("");
const selectedGroup = ref();
const selectedPolicy = ref();
const selectedSort = ref();
const page = ref(1);
const size = ref(60);

const {
  attachments,
  isLoading,
  total,
  selectedAttachment,
  selectedAttachments,
  handleFetchAttachments,
  handleSelect,
  handleSelectPrevious,
  handleSelectNext,
  handleReset,
  isChecked,
  isFetching,
} = useAttachmentControl({
  groupName: selectedGroup,
  policyName: selectedPolicy,
  sort: selectedSort,
  accepts: computed(() => {
    return props.accepts;
  }),
  page,
  size,
  keyword,
});

watch(
  () => [selectedPolicy.value, selectedSort.value, keyword.value],
  () => {
    page.value = 1;
  }
);

const hasFilters = computed(() => {
  return selectedPolicy.value || selectedSort.value;
});

function handleClearFilters() {
  selectedPolicy.value = undefined;
  selectedSort.value = undefined;
}

const uploadVisible = ref(false);
const detailVisible = ref(false);

watchEffect(() => {
  emit("update:selected", Array.from(selectedAttachments.value));
});

const handleOpenDetail = (attachment: Attachment) => {
  selectedAttachment.value = attachment;
  detailVisible.value = true;
};

const isDisabled = (attachment: Attachment) => {
  const isMatchMediaType = matchMediaTypes(
    attachment.spec.mediaType || "*/*",
    props.accepts
  );

  if (
    props.max !== undefined &&
    props.max <= selectedAttachments.value.size &&
    !isChecked(attachment)
  ) {
    return true;
  }

  return !isMatchMediaType;
};

function onUploadModalClose() {
  handleFetchAttachments();
  uploadVisible.value = false;
}

function onDetailModalClose() {
  detailVisible.value = false;
  selectedAttachment.value = undefined;
}

function onGroupSelect(group: Group) {
  selectedGroup.value = group.metadata.name;
  handleReset();
}

// View type
const viewTypes = [
  {
    name: "list",
    tooltip: t("core.attachment.filters.view_type.items.list"),
    icon: IconList,
  },
  {
    name: "grid",
    tooltip: t("core.attachment.filters.view_type.items.grid"),
    icon: IconGrid,
  },
];

const viewType = useLocalStorage("attachment-selector-view-type", "grid");
</script>
<template>
  <div class="mb-3 block w-full rounded bg-gray-50 px-3 py-2">
    <div class="relative flex flex-col items-start sm:flex-row sm:items-center">
      <div class="flex w-full flex-1 items-center sm:w-auto">
        <SearchInput v-model="keyword" />
      </div>
      <div class="mt-4 flex sm:mt-0">
        <VSpace spacing="lg">
          <FilterCleanButton v-if="hasFilters" @click="handleClearFilters" />

          <FilterDropdown
            v-model="selectedPolicy"
            :label="$t('core.attachment.filters.storage_policy.label')"
            :items="[
              {
                label: t('core.common.filters.item_labels.all'),
              },
              ...(policies?.map((policy) => {
                return {
                  label: policy.spec.displayName,
                  value: policy.metadata.name,
                };
              }) || []),
            ]"
          />

          <FilterDropdown
            v-model="selectedSort"
            :label="$t('core.common.filters.labels.sort')"
            :items="[
              {
                label: t('core.common.filters.item_labels.default'),
              },
              {
                label: t('core.attachment.filters.sort.items.create_time_desc'),
                value: 'metadata.creationTimestamp,desc',
              },
              {
                label: t('core.attachment.filters.sort.items.create_time_asc'),
                value: 'metadata.creationTimestamp,asc',
              },
              {
                label: t(
                  'core.attachment.filters.sort.items.display_name_desc'
                ),
                value: 'spec.displayName,desc',
              },
              {
                label: t('core.attachment.filters.sort.items.display_name_asc'),
                value: 'spec.displayName,asc',
              },
              {
                label: t('core.attachment.filters.sort.items.size_desc'),
                value: 'spec.size,desc',
              },
              {
                label: t('core.attachment.filters.sort.items.size_asc'),
                value: 'spec.size,asc',
              },
            ]"
          />

          <div class="flex flex-row gap-2">
            <div
              v-for="(item, index) in viewTypes"
              :key="index"
              v-tooltip="`${item.tooltip}`"
              :class="{
                'bg-gray-200 font-bold text-black': viewType === item.name,
              }"
              class="cursor-pointer rounded p-1 hover:bg-gray-200"
              @click="viewType = item.name"
            >
              <component :is="item.icon" class="h-4 w-4" />
            </div>
          </div>

          <div class="flex flex-row gap-2">
            <div
              class="group cursor-pointer rounded p-1 hover:bg-gray-200"
              @click="handleFetchAttachments()"
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

  <AttachmentGroupList readonly @select="onGroupSelect" />

  <HasPermission
    v-if="attachments?.length"
    :permissions="['system:attachments:manage']"
  >
    <div class="mb-5">
      <VButton @click="uploadVisible = true">
        <template #icon>
          <IconUpload class="h-full w-full" />
        </template>
        {{ $t("core.common.buttons.upload") }}
      </VButton>
    </div>
  </HasPermission>

  <VLoading v-if="isLoading" />

  <VEmpty
    v-else-if="!attachments?.length"
    :message="$t('core.attachment.empty.message')"
    :title="$t('core.attachment.empty.title')"
  >
    <template #actions>
      <VSpace>
        <VButton @click="handleFetchAttachments">
          {{ $t("core.common.buttons.refresh") }}
        </VButton>
        <VButton type="secondary" @click="uploadVisible = true">
          <template #icon>
            <IconUpload class="h-full w-full" />
          </template>
          {{ $t("core.attachment.empty.actions.upload") }}
        </VButton>
      </VSpace>
    </template>
  </VEmpty>

  <div v-else>
    <Transition v-if="viewType === 'grid'" appear name="fade">
      <div
        class="mt-2 grid grid-cols-3 gap-x-2 gap-y-3 sm:grid-cols-3 md:grid-cols-6 xl:grid-cols-8 2xl:grid-cols-10"
        role="list"
      >
        <VCard
          v-for="(attachment, index) in attachments"
          :key="index"
          :body-class="['!p-0']"
          :class="{
            'ring-1 ring-primary': isChecked(attachment),
            'pointer-events-none !cursor-not-allowed opacity-50':
              isDisabled(attachment),
          }"
          class="hover:shadow"
          @click.stop="handleSelect(attachment)"
        >
          <div class="group relative bg-white">
            <div
              class="aspect-h-8 aspect-w-10 block h-full w-full cursor-pointer overflow-hidden bg-gray-100"
            >
              <LazyImage
                v-if="isImage(attachment.spec.mediaType)"
                :key="attachment.metadata.name"
                :alt="attachment.spec.displayName"
                :src="
                  attachment.status?.thumbnails?.S ||
                  attachment.status?.permalink
                "
                classes="pointer-events-none object-cover group-hover:opacity-75 transform-gpu"
              >
                <template #loading>
                  <div
                    class="flex h-full items-center justify-center object-cover"
                  >
                    <span class="text-xs text-gray-400">
                      {{ $t("core.common.status.loading") }}...
                    </span>
                  </div>
                </template>
                <template #error>
                  <div
                    class="flex h-full items-center justify-center object-cover"
                  >
                    <span class="text-xs text-red-400">
                      {{ $t("core.common.status.loading_error") }}
                    </span>
                  </div>
                </template>
              </LazyImage>
              <AttachmentFileTypeIcon
                v-else
                :file-name="attachment.spec.displayName"
              />
            </div>
            <p
              class="pointer-events-none block truncate px-2 py-1 text-center text-xs font-medium text-gray-700"
            >
              {{ attachment.spec.displayName }}
            </p>

            <div
              :class="{ '!flex': selectedAttachments.has(attachment) }"
              class="absolute left-0 top-0 hidden h-1/3 w-full justify-end bg-gradient-to-b from-gray-300 to-transparent ease-in-out group-hover:flex"
            >
              <IconEye
                class="mr-1 mt-1 hidden h-6 w-6 cursor-pointer text-white transition-all hover:text-primary group-hover:block"
                @click.stop="handleOpenDetail(attachment)"
              />
              <IconCheckboxFill
                :class="{
                  '!text-primary': selectedAttachments.has(attachment),
                }"
                class="mr-1 mt-1 h-6 w-6 cursor-pointer text-white transition-all hover:text-primary"
              />
            </div>
          </div>
        </VCard>
      </div>
    </Transition>
    <Transition v-if="viewType === 'list'" appear name="fade">
      <ul
        class="box-border h-full w-full divide-y divide-gray-100 overflow-hidden rounded-base border"
        role="list"
      >
        <li v-for="attachment in attachments" :key="attachment.metadata.name">
          <AttachmentSelectorListItem
            :attachment="attachment"
            :is-selected="isChecked(attachment)"
            @select="handleSelect"
            @open-detail="handleOpenDetail"
          >
            <template #checkbox>
              <input
                :checked="isChecked(attachment)"
                :disabled="isDisabled(attachment)"
                type="checkbox"
                @click="handleSelect(attachment)"
              />
            </template>
          </AttachmentSelectorListItem>
        </li>
      </ul>
    </Transition>
  </div>

  <div class="mt-4">
    <VPagination
      v-model:page="page"
      v-model:size="size"
      :page-label="$t('core.components.pagination.page_label')"
      :size-label="$t('core.components.pagination.size_label')"
      :total-label="
        $t('core.components.pagination.total_label', { total: total })
      "
      :total="total"
      :size-options="[60, 120, 200]"
    />
  </div>
  <AttachmentUploadModal v-if="uploadVisible" @close="onUploadModalClose" />
  <AttachmentDetailModal
    v-if="detailVisible"
    :mount-to-body="true"
    :name="selectedAttachment?.metadata.name"
    @close="onDetailModalClose"
  >
    <template #actions>
      <span
        v-if="selectedAttachment && selectedAttachments.has(selectedAttachment)"
        @click="handleSelect(selectedAttachment)"
      >
        <IconCheckboxFill />
      </span>
      <span v-else @click="handleSelect(selectedAttachment)">
        <IconCheckboxCircle />
      </span>

      <span @click="handleSelectPrevious">
        <IconArrowLeft />
      </span>
      <span @click="handleSelectNext">
        <IconArrowRight />
      </span>
    </template>
  </AttachmentDetailModal>
</template>
