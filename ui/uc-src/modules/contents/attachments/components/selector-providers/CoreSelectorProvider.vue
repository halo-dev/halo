<script setup lang="ts">
import LazyImage from "@/components/image/LazyImage.vue";
import { isImage } from "@/utils/image";
import { matchMediaTypes } from "@/utils/media-type";
import { ucApiClient, type Attachment } from "@halo-dev/api-client";
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
import { useQuery } from "@tanstack/vue-query";
import { useLocalStorage } from "@vueuse/core";
import { computed, nextTick, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import AttachmentDetailModal from "../AttachmentDetailModal.vue";
import AttachmentUploadModal from "./AttachmentUploadModal.vue";
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

const page = ref(1);
const size = ref(60);
const keyword = ref("");
const selectedSort = ref();

const hasFilters = computed(() => {
  return selectedSort.value;
});

function handleClearFilters() {
  selectedSort.value = undefined;
}

const {
  data,
  isFetching,
  isLoading,
  refetch: handleFetchAttachments,
} = useQuery({
  queryKey: [
    "uc:attachments:my",
    props.accepts,
    page,
    size,
    keyword,
    selectedSort,
  ],
  queryFn: async () => {
    const { data } = await ucApiClient.storage.attachment.listMyAttachments({
      accepts: props.accepts,
      page: page.value,
      size: size.value,
      keyword: keyword.value,
      sort: [selectedSort.value],
    });
    return data;
  },
});

// Upload
const uploadVisible = ref(false);

function onUploadModalClose() {
  handleFetchAttachments();
  uploadVisible.value = false;
}

// Select
const selectedAttachment = ref<Attachment>();
const selectedAttachments = ref<Set<Attachment>>(new Set<Attachment>());

watch(
  () => selectedAttachments.value,
  (newValue) => {
    emit("update:selected", Array.from(newValue));
  },
  {
    deep: true,
  }
);

const isChecked = (attachment: Attachment) => {
  return (
    attachment.metadata.name === selectedAttachment.value?.metadata.name ||
    Array.from(selectedAttachments.value)
      .map((item) => item.metadata.name)
      .includes(attachment.metadata.name)
  );
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

const handleSelect = async (attachment: Attachment | undefined) => {
  if (!attachment) return;
  if (selectedAttachments.value.has(attachment)) {
    selectedAttachments.value.delete(attachment);
    return;
  }
  selectedAttachments.value.add(attachment);
};

// View type
const viewTypes = [
  {
    name: "list",
    tooltip: t("core.uc_attachment.filters.view_type.items.list"),
    icon: IconList,
  },
  {
    name: "grid",
    tooltip: t("core.uc_attachment.filters.view_type.items.grid"),
    icon: IconGrid,
  },
];

const viewType = useLocalStorage("attachment-selector-view-type", "grid");

// Detail modal
function handleOpenDetail(attachment: Attachment) {
  selectedAttachment.value = attachment;
}

function onDetailModalClose() {
  selectedAttachment.value = undefined;
}

const handleSelectPrevious = async () => {
  if (!data.value) return;

  const index = data.value.items.findIndex(
    (attachment) =>
      attachment.metadata.name === selectedAttachment.value?.metadata.name
  );

  if (index === undefined) return;

  if (index > 0) {
    selectedAttachment.value = data.value.items[index - 1];
    return;
  }

  if (index === 0 && data.value.hasPrevious) {
    page.value--;
    await nextTick();
    await handleFetchAttachments();
    selectedAttachment.value = data.value.items[data.value.items.length - 1];
  }
};

const handleSelectNext = async () => {
  if (!data.value) return;

  const index = data.value.items.findIndex(
    (attachment) =>
      attachment.metadata.name === selectedAttachment.value?.metadata.name
  );

  if (index === undefined) return;

  if (index < data.value.items.length - 1) {
    selectedAttachment.value = data.value.items[index + 1];
    return;
  }

  if (index === data.value.items.length - 1 && data.value.hasNext) {
    page.value++;
    await nextTick();
    await handleFetchAttachments();
    selectedAttachment.value = data.value.items[0];
  }
};
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
                label: t(
                  'core.uc_attachment.filters.sort.items.create_time_desc'
                ),
                value: 'metadata.creationTimestamp,desc',
              },
              {
                label: t(
                  'core.uc_attachment.filters.sort.items.create_time_asc'
                ),
                value: 'metadata.creationTimestamp,asc',
              },
              {
                label: t(
                  'core.uc_attachment.filters.sort.items.display_name_desc'
                ),
                value: 'spec.displayName,desc',
              },
              {
                label: t(
                  'core.uc_attachment.filters.sort.items.display_name_asc'
                ),
                value: 'spec.displayName,asc',
              },
              {
                label: t('core.uc_attachment.filters.sort.items.size_desc'),
                value: 'spec.size,desc',
              },
              {
                label: t('core.uc_attachment.filters.sort.items.size_asc'),
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

  <div v-if="data?.total" class="mb-5">
    <VButton @click="uploadVisible = true">
      <template #icon>
        <IconUpload class="h-full w-full" />
      </template>
      {{ $t("core.common.buttons.upload") }}
    </VButton>
  </div>

  <VLoading v-if="isLoading" />

  <VEmpty
    v-else-if="!data?.total"
    :message="$t('core.uc_attachment.empty.message')"
    :title="$t('core.uc_attachment.empty.title')"
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
          {{ $t("core.uc_attachment.empty.actions.upload") }}
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
          v-for="(attachment, index) in data.items"
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
        <li v-for="attachment in data.items" :key="attachment.metadata.name">
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
        $t('core.components.pagination.total_label', {
          total: data?.total || 0,
        })
      "
      :total="data?.total || 0"
      :size-options="[60, 120, 200]"
    />
  </div>

  <AttachmentUploadModal v-if="uploadVisible" @close="onUploadModalClose" />

  <AttachmentDetailModal
    v-if="selectedAttachment"
    :mount-to-body="true"
    :attachment="selectedAttachment"
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
