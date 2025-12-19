<script lang="ts" setup>
import AttachmentGridListItem from "@/components/attachment/AttachmentGridListItem.vue";
import { matchMediaTypes } from "@/utils/media-type";
import type { Attachment } from "@halo-dev/api-client";
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
  VEmpty,
  VEntityContainer,
  VLoading,
  VPagination,
  VSpace,
} from "@halo-dev/components";
import type { AttachmentLike } from "@halo-dev/ui-shared";
import { useLocalStorage } from "@vueuse/core";
import { throttle } from "es-toolkit/compat";
import { computed, ref, watch, watchEffect } from "vue";
import { useI18n } from "vue-i18n";
import { useAttachmentControl } from "../../composables/use-attachment";
import AttachmentDetailModal from "../AttachmentDetailModal.vue";
import AttachmentUploadArea from "../AttachmentUploadArea.vue";
import AttachmentSelectorListItem from "./components/AttachmentSelectorListItem.vue";
import GroupFilter from "./components/GroupFilter.vue";
import PolicyFilter from "./components/PolicyFilter.vue";

const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    selected: AttachmentLike[];
    accepts?: string[];
    min?: number;
    max?: number;
  }>(),
  {
    accepts: () => ["*/*"],
    min: undefined,
    max: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:selected", attachments: AttachmentLike[]): void;
  (event: "change-provider", providerId: string): void;
}>();

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
  selectedAttachmentNames,
  handleFetchAttachments,
  handleSelect,
  handleSelectPrevious,
  handleSelectNext,
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

const throttledFetchAttachments = throttle(handleFetchAttachments, 1000, {
  leading: false,
  trailing: true,
});

watch(
  () => [
    selectedPolicy.value,
    selectedSort.value,
    keyword.value,
    selectedGroup.value,
  ],
  () => {
    page.value = 1;
  }
);

const hasFilters = computed(() => {
  return selectedPolicy.value || selectedSort.value || selectedGroup.value;
});

function handleClearFilters() {
  selectedPolicy.value = undefined;
  selectedSort.value = undefined;
  selectedGroup.value = undefined;
}

const uploadVisible = ref(false);

watchEffect(() => {
  emit("update:selected", Array.from(selectedAttachments.value));
});

const handleOpenDetail = (attachment: Attachment) => {
  selectedAttachment.value = attachment;
};

const isDisabled = (attachment: Attachment) => {
  const isMatchMediaType = matchMediaTypes(
    attachment.spec.mediaType || "*/*",
    props.accepts
  );

  if (
    props.max !== undefined &&
    props.max <= selectedAttachmentNames.value.size &&
    !isChecked(attachment)
  ) {
    return true;
  }

  return !isMatchMediaType;
};

function onDetailModalClose() {
  selectedAttachment.value = undefined;
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

function onUploadDone() {
  handleFetchAttachments();
  uploadVisible.value = false;
}

function onUploaded(attachment: Attachment) {
  handleSelect(attachment);
  page.value = 1;
  throttledFetchAttachments();
}
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
              v-for="item in viewTypes"
              :key="item.name"
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

  <div class="space-y-4">
    <PolicyFilter v-model="selectedPolicy" />
    <GroupFilter v-model="selectedGroup" />
  </div>

  <HasPermission :permissions="['system:attachments:manage']">
    <div class="my-5 space-y-3">
      <VButton @click="uploadVisible = !uploadVisible">
        <template #icon>
          <IconUpload />
        </template>
        {{ uploadVisible ? "取消上传" : $t("core.common.buttons.upload") }}
      </VButton>

      <Transition v-if="uploadVisible" appear name="fade">
        <AttachmentUploadArea
          :policy-name="selectedPolicy"
          :group-name="selectedGroup"
          height="450px"
          @done="onUploadDone"
          @uploaded="onUploaded"
        />
      </Transition>
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
            <IconUpload />
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
        <AttachmentGridListItem
          v-for="attachment in attachments"
          :key="attachment.metadata.name"
          :attachment="attachment"
          :is-selected="isChecked(attachment)"
          :is-disabled="isDisabled(attachment)"
          @select="handleSelect(attachment)"
          @click="handleSelect(attachment)"
        >
          <template #actions>
            <IconEye
              class="mr-1 mt-1 hidden h-6 w-6 cursor-pointer text-white transition-all hover:text-primary group-hover:block"
              @click.stop="handleOpenDetail(attachment)"
            />
          </template>
        </AttachmentGridListItem>
      </div>
    </Transition>
    <Transition v-if="viewType === 'list'" appear name="fade">
      <div class="overflow-hidden rounded-base border">
        <VEntityContainer>
          <AttachmentSelectorListItem
            v-for="attachment in attachments"
            :key="attachment.metadata.name"
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
        </VEntityContainer>
      </div>
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
  <AttachmentDetailModal
    v-if="selectedAttachment"
    :mount-to-body="true"
    :name="selectedAttachment?.metadata.name"
    @close="onDetailModalClose"
  >
    <template #actions>
      <span
        v-if="isChecked(selectedAttachment)"
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
