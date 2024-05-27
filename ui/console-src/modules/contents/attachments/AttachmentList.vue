<script lang="ts" setup>
import UserFilterDropdown from "@/components/filter/UserFilterDropdown.vue";
import LazyImage from "@/components/image/LazyImage.vue";
import { apiClient } from "@/utils/api-client";
import { isImage } from "@/utils/image";
import type { Attachment, Group } from "@halo-dev/api-client";
import {
  IconArrowLeft,
  IconArrowRight,
  IconCheckboxFill,
  IconDatabase2Line,
  IconFolder,
  IconGrid,
  IconList,
  IconRefreshLine,
  IconUpload,
  Toast,
  VButton,
  VCard,
  VDropdown,
  VDropdownItem,
  VEmpty,
  VLoading,
  VPageHeader,
  VPagination,
  VSpace,
} from "@halo-dev/components";
import { useLocalStorage } from "@vueuse/core";
import { useRouteQuery } from "@vueuse/router";
import { cloneDeep } from "lodash-es";
import type { Ref } from "vue";
import { computed, onMounted, provide, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import AttachmentDetailModal from "./components/AttachmentDetailModal.vue";
import AttachmentGroupList from "./components/AttachmentGroupList.vue";
import AttachmentListItem from "./components/AttachmentListItem.vue";
import AttachmentPoliciesModal from "./components/AttachmentPoliciesModal.vue";
import AttachmentUploadModal from "./components/AttachmentUploadModal.vue";
import { useAttachmentControl } from "./composables/use-attachment";
import { useFetchAttachmentGroup } from "./composables/use-attachment-group";
import { useFetchAttachmentPolicy } from "./composables/use-attachment-policy";

const { t } = useI18n();

const policyVisible = ref(false);
const uploadVisible = ref(false);
const detailVisible = ref(false);

const { policies } = useFetchAttachmentPolicy();
const { groups } = useFetchAttachmentGroup();

const selectedGroup = useRouteQuery<string | undefined>("group");

// Filter
const keyword = useRouteQuery<string>("keyword", "");
const page = useRouteQuery<number>("page", 1, {
  transform: Number,
});
const size = useRouteQuery<number>("size", 60, {
  transform: Number,
});
const selectedPolicy = useRouteQuery<string | undefined>("policy");
const selectedUser = useRouteQuery<string | undefined>("user");
const selectedSort = useRouteQuery<string | undefined>("sort");
const selectedAccepts = useRouteQuery<string | undefined>("accepts");

watch(
  () => [
    selectedPolicy.value,
    selectedUser.value,
    selectedSort.value,
    selectedAccepts.value,
    keyword.value,
  ],
  () => {
    page.value = 1;
  }
);

const hasFilters = computed(() => {
  return (
    selectedPolicy.value ||
    selectedUser.value ||
    selectedSort.value ||
    selectedAccepts.value
  );
});

function handleClearFilters() {
  selectedPolicy.value = undefined;
  selectedUser.value = undefined;
  selectedSort.value = undefined;
  selectedAccepts.value = undefined;
}

const {
  attachments,
  selectedAttachment,
  selectedAttachments,
  checkedAll,
  isLoading,
  isFetching,
  total,
  handleFetchAttachments,
  handleSelectNext,
  handleSelectPrevious,
  handleDeleteInBatch,
  handleCheckAll,
  handleSelect,
  isChecked,
  handleReset,
} = useAttachmentControl({
  groupName: selectedGroup,
  policyName: selectedPolicy,
  user: selectedUser,
  accepts: computed(() => {
    if (!selectedAccepts.value) {
      return [];
    }
    return selectedAccepts.value.split(",");
  }),
  keyword: keyword,
  sort: selectedSort,
  page: page,
  size: size,
});

provide<Ref<Set<Attachment>>>("selectedAttachments", selectedAttachments);

const handleMove = async (group: Group) => {
  try {
    const promises = Array.from(selectedAttachments.value).map((attachment) => {
      const attachmentToUpdate = cloneDeep(attachment);
      attachmentToUpdate.spec.groupName = group.metadata.name;
      return apiClient.extension.storage.attachment.updateStorageHaloRunV1alpha1Attachment(
        {
          name: attachment.metadata.name,
          attachment: attachmentToUpdate,
        }
      );
    });

    await Promise.all(promises);
    selectedAttachments.value.clear();

    Toast.success(t("core.attachment.operations.move.toast_success"));
  } catch (e) {
    console.error(e);
  } finally {
    handleFetchAttachments();
  }
};

const handleClickItem = (attachment: Attachment) => {
  if (attachment.metadata.deletionTimestamp) {
    return;
  }

  if (selectedAttachments.value.size > 0) {
    handleSelect(attachment);
    return;
  }

  selectedAttachment.value = attachment;
  selectedAttachments.value.clear();
  detailVisible.value = true;
};

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;
  handleCheckAll(checked);
};

const onDetailModalClose = () => {
  selectedAttachment.value = undefined;
  nameQuery.value = undefined;
  nameQueryAttachment.value = undefined;
  detailVisible.value = false;
  handleFetchAttachments();
};

const onUploadModalClose = () => {
  routeQueryAction.value = undefined;
  handleFetchAttachments();
  uploadVisible.value = false;
};

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

const viewType = useLocalStorage("attachment-view-type", "list");

// Route query action
const routeQueryAction = useRouteQuery<string | undefined>("action");

onMounted(() => {
  if (!routeQueryAction.value) {
    return;
  }
  if (routeQueryAction.value === "upload") {
    uploadVisible.value = true;
  }
});

const nameQuery = useRouteQuery<string | undefined>("name");
const nameQueryAttachment = ref<Attachment>();

watch(
  () => selectedAttachment.value,
  () => {
    if (selectedAttachment.value) {
      nameQuery.value = selectedAttachment.value.metadata.name;
    }
  }
);

onMounted(() => {
  if (!nameQuery.value) {
    return;
  }
  apiClient.extension.storage.attachment
    .getStorageHaloRunV1alpha1Attachment({
      name: nameQuery.value,
    })
    .then((response) => {
      nameQueryAttachment.value = response.data;
      detailVisible.value = true;
    });
});
</script>
<template>
  <AttachmentDetailModal
    v-if="detailVisible"
    :attachment="selectedAttachment || nameQueryAttachment"
    @close="onDetailModalClose"
  >
    <template #actions>
      <span @click="handleSelectPrevious">
        <IconArrowLeft />
      </span>
      <span @click="handleSelectNext">
        <IconArrowRight />
      </span>
    </template>
  </AttachmentDetailModal>
  <AttachmentUploadModal v-if="uploadVisible" @close="onUploadModalClose" />
  <AttachmentPoliciesModal
    v-if="policyVisible"
    @close="policyVisible = false"
  />
  <VPageHeader :title="$t('core.attachment.title')">
    <template #icon>
      <IconFolder class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <VButton
          v-permission="['system:attachments:manage']"
          size="sm"
          @click="policyVisible = true"
        >
          <template #icon>
            <IconDatabase2Line class="h-full w-full" />
          </template>
          {{ $t("core.attachment.actions.storage_policies") }}
        </VButton>
        <VButton
          v-permission="['system:attachments:manage']"
          type="secondary"
          @click="uploadVisible = true"
        >
          <template #icon>
            <IconUpload class="h-full w-full" />
          </template>
          {{ $t("core.common.buttons.upload") }}
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <div class="flex flex-col gap-2 sm:flex-row">
      <div class="w-full">
        <VCard :body-class="[viewType === 'list' ? '!p-0' : '']">
          <template #header>
            <div class="block w-full bg-gray-50 px-4 py-3">
              <div
                class="relative flex flex-col flex-wrap items-start gap-4 sm:flex-row sm:items-center"
              >
                <div
                  v-permission="['system:attachments:manage']"
                  class="hidden items-center sm:flex"
                >
                  <input
                    v-model="checkedAll"
                    type="checkbox"
                    @change="handleCheckAllChange"
                  />
                </div>
                <div class="flex w-full flex-1 items-center sm:w-auto">
                  <SearchInput
                    v-if="!selectedAttachments.size"
                    v-model="keyword"
                  />
                  <VSpace v-else>
                    <VButton type="danger" @click="handleDeleteInBatch">
                      {{ $t("core.common.buttons.delete") }}
                    </VButton>
                    <VButton @click="selectedAttachments.clear()">
                      {{
                        $t("core.attachment.operations.deselect_items.button")
                      }}
                    </VButton>
                    <VDropdown>
                      <VButton>
                        {{ $t("core.attachment.operations.move.button") }}
                      </VButton>
                      <template #popper>
                        <VDropdownItem
                          v-for="(group, index) in groups"
                          :key="index"
                          @click="handleMove(group)"
                        >
                          {{ group.spec.displayName }}
                        </VDropdownItem>
                      </template>
                    </VDropdown>
                  </VSpace>
                </div>
                <VSpace spacing="lg" class="flex-wrap">
                  <FilterCleanButton
                    v-if="hasFilters"
                    @click="handleClearFilters"
                  />
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
                    v-model="selectedAccepts"
                    :label="$t('core.attachment.filters.accept.label')"
                    :items="[
                      {
                        label: t('core.common.filters.item_labels.all'),
                      },
                      {
                        label: t('core.attachment.filters.accept.items.image'),
                        value: 'image/*',
                      },
                      {
                        label: t('core.attachment.filters.accept.items.audio'),
                        value: 'audio/*',
                      },
                      {
                        label: t('core.attachment.filters.accept.items.video'),
                        value: 'video/*',
                      },
                      {
                        label: t('core.attachment.filters.accept.items.file'),
                        value: 'text/*,application/*',
                      },
                    ]"
                  />
                  <HasPermission :permissions="['system:users:view']">
                    <UserFilterDropdown
                      v-model="selectedUser"
                      :label="$t('core.attachment.filters.owner.label')"
                    />
                  </HasPermission>
                  <FilterDropdown
                    v-model="selectedSort"
                    :label="$t('core.common.filters.labels.sort')"
                    :items="[
                      {
                        label: t('core.common.filters.item_labels.default'),
                      },
                      {
                        label: t(
                          'core.attachment.filters.sort.items.create_time_desc'
                        ),
                        value: 'metadata.creationTimestamp,desc',
                      },
                      {
                        label: t(
                          'core.attachment.filters.sort.items.create_time_asc'
                        ),
                        value: 'metadata.creationTimestamp,asc',
                      },
                      {
                        label: t(
                          'core.attachment.filters.sort.items.display_name_desc'
                        ),
                        value: 'spec.displayName,desc',
                      },
                      {
                        label: t(
                          'core.attachment.filters.sort.items.display_name_asc'
                        ),
                        value: 'spec.displayName,asc',
                      },
                      {
                        label: t(
                          'core.attachment.filters.sort.items.size_desc'
                        ),
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
                        'bg-gray-200 font-bold text-black':
                          viewType === item.name,
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
          </template>

          <div :style="`${viewType === 'list' ? 'padding:12px 16px 0' : ''}`">
            <AttachmentGroupList @select="handleReset" />
          </div>

          <VLoading v-if="isLoading" />

          <Transition v-else-if="!attachments?.length" appear name="fade">
            <VEmpty
              :message="$t('core.attachment.empty.message')"
              :title="$t('core.attachment.empty.title')"
            >
              <template #actions>
                <VSpace>
                  <VButton @click="handleFetchAttachments">
                    {{ $t("core.common.buttons.refresh") }}
                  </VButton>
                  <VButton
                    v-permission="['system:attachments:manage']"
                    type="secondary"
                    @click="uploadVisible = true"
                  >
                    <template #icon>
                      <IconUpload class="h-full w-full" />
                    </template>
                    {{ $t("core.attachment.empty.actions.upload") }}
                  </VButton>
                </VSpace>
              </template>
            </VEmpty>
          </Transition>

          <div v-else>
            <Transition v-if="viewType === 'grid'" appear name="fade">
              <div
                class="mt-2 grid grid-cols-3 gap-x-2 gap-y-3 sm:grid-cols-3 md:grid-cols-6 xl:grid-cols-8 2xl:grid-cols-12"
                role="list"
              >
                <VCard
                  v-for="attachment in attachments"
                  :key="attachment.metadata.name"
                  :body-class="['!p-0']"
                  :class="{
                    'ring-1 ring-primary': isChecked(attachment),
                    'ring-1 ring-red-600':
                      attachment.metadata.deletionTimestamp,
                  }"
                  class="hover:shadow"
                  @click="handleClickItem(attachment)"
                >
                  <div class="group relative bg-white">
                    <div
                      class="aspect-h-8 aspect-w-10 block h-full w-full cursor-pointer overflow-hidden bg-gray-100"
                    >
                      <LazyImage
                        v-if="isImage(attachment.spec.mediaType)"
                        :key="attachment.metadata.name"
                        :alt="attachment.spec.displayName"
                        :src="attachment.status?.permalink"
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
                      v-tooltip="attachment.spec.displayName"
                      class="block cursor-pointer truncate px-2 py-1 text-center text-xs font-medium text-gray-700"
                    >
                      {{ attachment.spec.displayName }}
                    </p>

                    <div
                      v-if="attachment.metadata.deletionTimestamp"
                      class="absolute right-1 top-1 text-xs text-red-300"
                    >
                      {{ $t("core.common.status.deleting") }}...
                    </div>

                    <div
                      v-if="!attachment.metadata.deletionTimestamp"
                      v-permission="['system:attachments:manage']"
                      :class="{ '!flex': selectedAttachments.has(attachment) }"
                      class="absolute left-0 top-0 hidden h-1/3 w-full cursor-pointer justify-end bg-gradient-to-b from-gray-300 to-transparent ease-in-out group-hover:flex"
                    >
                      <IconCheckboxFill
                        :class="{
                          '!text-primary': selectedAttachments.has(attachment),
                        }"
                        class="mr-1 mt-1 h-6 w-6 cursor-pointer text-white transition-all hover:text-primary"
                        @click.stop="handleSelect(attachment)"
                      />
                    </div>
                  </div>
                </VCard>
              </div>
            </Transition>
            <Transition v-if="viewType === 'list'" appear name="fade">
              <ul
                class="box-border h-full w-full divide-y divide-gray-100"
                role="list"
              >
                <li
                  v-for="attachment in attachments"
                  :key="attachment.metadata.name"
                >
                  <AttachmentListItem
                    :attachment="attachment"
                    :is-selected="isChecked(attachment)"
                    @select="handleSelect"
                    @open-detail="handleClickItem"
                  />
                </li>
              </ul>
            </Transition>
          </div>

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
              :size-options="[60, 120, 200]"
            />
          </template>
        </VCard>
      </div>
    </div>
  </div>
</template>
