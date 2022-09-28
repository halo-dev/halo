<script lang="ts" setup>
import {
  IconArrowDown,
  IconArrowLeft,
  IconArrowRight,
  IconCheckboxFill,
  IconDatabase2Line,
  IconGrid,
  IconList,
  IconUpload,
  VButton,
  VCard,
  VPageHeader,
  VPagination,
  VSpace,
  VEmpty,
  IconCloseCircle,
  IconFolder,
  VStatusDot,
  VEntity,
  VEntityField,
} from "@halo-dev/components";
import LazyImage from "@/components/image/LazyImage.vue";
import UserDropdownSelector from "@/components/dropdown-selector/UserDropdownSelector.vue";
import AttachmentDetailModal from "./components/AttachmentDetailModal.vue";
import AttachmentUploadModal from "./components/AttachmentUploadModal.vue";
import AttachmentPoliciesModal from "./components/AttachmentPoliciesModal.vue";
import AttachmentGroupList from "./components/AttachmentGroupList.vue";
import { onMounted, ref, watch } from "vue";
import type { Attachment, Group, Policy, User } from "@halo-dev/api-client";
import { formatDatetime } from "@/utils/date";
import prettyBytes from "pretty-bytes";
import { useFetchAttachmentPolicy } from "./composables/use-attachment-policy";
import { useAttachmentControl } from "./composables/use-attachment";
import AttachmentFileTypeIcon from "./components/AttachmentFileTypeIcon.vue";
import { apiClient } from "@/utils/api-client";
import cloneDeep from "lodash.clonedeep";
import { isImage } from "@/utils/image";
import { useRouteQuery } from "@vueuse/router";
import { useFetchAttachmentGroup } from "./composables/use-attachment-group";

const policyVisible = ref(false);
const uploadVisible = ref(false);
const detailVisible = ref(false);

const { policies } = useFetchAttachmentPolicy({ fetchOnMounted: true });
const { groups, handleFetchGroups } = useFetchAttachmentGroup({
  fetchOnMounted: true,
});

const selectedGroup = ref<Group>();

// Filter
const selectedPolicy = ref<Policy>();
const selectedUser = ref<User>();
const keyword = ref<string>("");

function handleSelectPolicy(policy: Policy | undefined) {
  selectedPolicy.value = policy;
  handleFetchAttachments();
}

function handleSelectUser(user: User | undefined) {
  selectedUser.value = user;
  handleFetchAttachments();
}

const {
  attachments,
  selectedAttachment,
  selectedAttachments,
  checkedAll,
  loading,
  handleFetchAttachments,
  handleSelectNext,
  handleSelectPrevious,
  handlePaginationChange,
  handleDelete,
  handleDeleteInBatch,
  handleCheckAll,
  handleSelect,
  isChecked,
  handleReset,
} = useAttachmentControl({
  group: selectedGroup,
  policy: selectedPolicy,
  user: selectedUser,
  keyword: keyword,
});

const handleMove = async (group: Group) => {
  try {
    const promises = Array.from(selectedAttachments.value).map((attachment) => {
      const attachmentToUpdate = cloneDeep(attachment);
      attachmentToUpdate.spec.groupRef = {
        name: group.metadata.name,
      };
      return apiClient.extension.storage.attachment.updatestorageHaloRunV1alpha1Attachment(
        {
          name: attachment.metadata.name,
          attachment: attachmentToUpdate,
        }
      );
    });

    await Promise.all(promises);
    selectedAttachments.value.clear();
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
  handleFetchAttachments();
};

const onUploadModalClose = () => {
  routeQueryAction.value = undefined;
  handleFetchAttachments();
};

const onGroupChange = () => {
  handleReset();
  handleFetchAttachments();
};

const getPolicyName = (name: string | undefined) => {
  const policy = policies.value.find((p) => p.metadata.name === name);
  return policy?.spec.displayName;
};

// View type
const viewTypes = [
  {
    name: "list",
    icon: IconList,
  },
  {
    name: "grid",
    icon: IconGrid,
  },
];

const viewType = useRouteQuery<string>("view", "grid");

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
    .getstorageHaloRunV1alpha1Attachment({
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
    v-model:visible="detailVisible"
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
  <AttachmentUploadModal
    v-model:visible="uploadVisible"
    :group="selectedGroup"
    @close="onUploadModalClose"
  />
  <AttachmentPoliciesModal v-model:visible="policyVisible" />
  <VPageHeader title="附件库">
    <template #icon>
      <IconFolder class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <VButton size="sm" @click="policyVisible = true">
          <template #icon>
            <IconDatabase2Line class="h-full w-full" />
          </template>
          存储策略
        </VButton>
        <VButton type="secondary" @click="uploadVisible = true">
          <template #icon>
            <IconUpload class="h-full w-full" />
          </template>
          上传
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
                class="relative flex flex-col items-start sm:flex-row sm:items-center"
              >
                <div class="mr-4 hidden items-center sm:flex">
                  <input
                    v-model="checkedAll"
                    class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                    type="checkbox"
                    @change="handleCheckAllChange"
                  />
                </div>
                <div class="flex w-full flex-1 items-center sm:w-auto">
                  <div
                    v-if="!selectedAttachments.size"
                    class="flex items-center gap-2"
                  >
                    <FormKit
                      v-model="keyword"
                      placeholder="输入关键词搜索"
                      type="text"
                      @keyup.enter="handleFetchAttachments()"
                    ></FormKit>

                    <div
                      v-if="selectedPolicy"
                      class="group flex cursor-pointer items-center justify-center gap-1 rounded-full bg-gray-200 px-2 py-1 hover:bg-gray-300"
                    >
                      <span
                        class="text-xs text-gray-600 group-hover:text-gray-900"
                      >
                        存储策略：{{ selectedPolicy?.spec.displayName }}
                      </span>
                      <IconCloseCircle
                        class="h-4 w-4 text-gray-600"
                        @click="handleSelectPolicy(undefined)"
                      />
                    </div>

                    <div
                      v-if="selectedUser"
                      class="group flex cursor-pointer items-center justify-center gap-1 rounded-full bg-gray-200 px-2 py-1 hover:bg-gray-300"
                    >
                      <span
                        class="text-xs text-gray-600 group-hover:text-gray-900"
                      >
                        上传者：{{ selectedUser?.spec.displayName }}
                      </span>
                      <IconCloseCircle
                        class="h-4 w-4 text-gray-600"
                        @click="handleSelectUser(undefined)"
                      />
                    </div>
                  </div>
                  <VSpace v-else>
                    <VButton type="danger" @click="handleDeleteInBatch">
                      删除
                    </VButton>
                    <VButton @click="selectedAttachments.clear()">
                      取消选择
                    </VButton>
                    <FloatingDropdown>
                      <VButton>移动</VButton>
                      <template #popper>
                        <div class="w-72 p-4">
                          <ul class="space-y-1">
                            <li
                              v-for="(group, index) in groups"
                              :key="index"
                              v-close-popper
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                              @click="handleMove(group)"
                            >
                              <span class="truncate">
                                {{ group.spec.displayName }}
                              </span>
                            </li>
                          </ul>
                        </div>
                      </template>
                    </FloatingDropdown>
                  </VSpace>
                </div>
                <div class="mt-4 flex sm:mt-0">
                  <VSpace spacing="lg">
                    <FloatingDropdown>
                      <div
                        class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                      >
                        <span class="mr-0.5">存储策略</span>
                        <span>
                          <IconArrowDown />
                        </span>
                      </div>
                      <template #popper>
                        <div class="w-72 p-4">
                          <ul class="space-y-1">
                            <li
                              v-for="(policy, index) in policies"
                              :key="index"
                              v-close-popper
                              :class="{
                                'bg-gray-100':
                                  selectedPolicy?.metadata.name ===
                                  policy.metadata.name,
                              }"
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                              @click="handleSelectPolicy(policy)"
                            >
                              <span class="truncate">
                                {{ policy.spec.displayName }}
                              </span>
                            </li>
                          </ul>
                        </div>
                      </template>
                    </FloatingDropdown>
                    <UserDropdownSelector
                      v-model:selected="selectedUser"
                      @select="handleSelectUser"
                    >
                      <div
                        class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                      >
                        <span class="mr-0.5">上传者</span>
                        <span>
                          <IconArrowDown />
                        </span>
                      </div>
                    </UserDropdownSelector>
                    <!-- TODO: add filter by ref support -->
                    <FloatingDropdown v-if="false">
                      <div
                        class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                      >
                        <span class="mr-0.5">引用位置</span>
                        <span>
                          <IconArrowDown />
                        </span>
                      </div>
                      <template #popper>
                        <div class="w-72 p-4">
                          <ul class="space-y-1">
                            <li
                              v-close-popper
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">未被引用</span>
                            </li>
                            <li
                              v-close-popper
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">文章</span>
                            </li>
                          </ul>
                        </div>
                      </template>
                    </FloatingDropdown>
                    <FloatingDropdown>
                      <div
                        class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                      >
                        <span class="mr-0.5">排序</span>
                        <span>
                          <IconArrowDown />
                        </span>
                      </div>
                      <template #popper>
                        <div class="w-72 p-4">
                          <ul class="space-y-1">
                            <li
                              v-close-popper
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">较近上传</span>
                            </li>
                            <li
                              v-close-popper
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">较晚上传</span>
                            </li>
                            <li
                              v-close-popper
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">文件大小降序</span>
                            </li>
                            <li
                              v-close-popper
                              class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                            >
                              <span class="truncate">文件大小升序</span>
                            </li>
                          </ul>
                        </div>
                      </template>
                    </FloatingDropdown>
                    <div class="flex flex-row gap-2">
                      <div
                        v-for="(item, index) in viewTypes"
                        :key="index"
                        :class="{
                          'bg-gray-200 font-bold text-black':
                            viewType === item.name,
                        }"
                        class="cursor-pointer rounded p-1 hover:bg-gray-200"
                        @click="viewType = item.name"
                      >
                        <component :is="item.icon" />
                      </div>
                    </div>
                  </VSpace>
                </div>
              </div>
            </div>
          </template>

          <div :style="`${viewType === 'list' ? 'padding:12px 16px 0' : ''}`">
            <AttachmentGroupList
              v-model:selected-group="selectedGroup"
              @select="onGroupChange"
              @update="handleFetchGroups"
            />
          </div>

          <VEmpty
            v-if="!attachments.total && !loading"
            message="当前分组没有附件，你可以尝试刷新或者上传附件"
            title="当前分组没有附件"
          >
            <template #actions>
              <VSpace>
                <VButton @click="handleFetchAttachments">刷新</VButton>
                <VButton type="secondary" @click="uploadVisible = true">
                  <template #icon>
                    <IconUpload class="h-full w-full" />
                  </template>
                  上传附件
                </VButton>
              </VSpace>
            </template>
          </VEmpty>

          <div v-else>
            <div v-if="viewType === 'grid'">
              <div
                class="mt-2 grid grid-cols-3 gap-x-2 gap-y-3 sm:grid-cols-3 md:grid-cols-6 xl:grid-cols-8 2xl:grid-cols-12"
                role="list"
              >
                <VCard
                  v-for="(attachment, index) in attachments.items"
                  :key="index"
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
                      class="aspect-w-10 aspect-h-8 block h-full w-full cursor-pointer overflow-hidden bg-gray-100"
                    >
                      <LazyImage
                        v-if="isImage(attachment.spec.mediaType)"
                        :key="attachment.metadata.name"
                        :alt="attachment.spec.displayName"
                        :src="attachment.status?.permalink"
                        classes="pointer-events-none object-cover group-hover:opacity-75"
                      >
                        <template #loading>
                          <div
                            class="flex h-full items-center justify-center object-cover"
                          >
                            <span class="text-xs text-gray-400">加载中...</span>
                          </div>
                        </template>
                        <template #error>
                          <div
                            class="flex h-full items-center justify-center object-cover"
                          >
                            <span class="text-xs text-red-400">加载异常</span>
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
                      class="absolute top-1 right-1 text-xs text-red-300"
                    >
                      删除中...
                    </div>

                    <div
                      v-if="!attachment.metadata.deletionTimestamp"
                      :class="{ '!flex': selectedAttachments.has(attachment) }"
                      class="absolute top-0 left-0 hidden h-1/3 w-full cursor-pointer justify-end bg-gradient-to-b from-gray-300 to-transparent ease-in-out group-hover:flex"
                    >
                      <IconCheckboxFill
                        :class="{
                          '!text-primary': selectedAttachments.has(attachment),
                        }"
                        class="mt-1 mr-1 h-6 w-6 cursor-pointer text-white transition-all hover:text-primary"
                        @click.stop="handleSelect(attachment)"
                      />
                    </div>
                  </div>
                </VCard>
              </div>
            </div>

            <ul
              v-if="viewType === 'list'"
              class="box-border h-full w-full divide-y divide-gray-100"
              role="list"
            >
              <li v-for="(attachment, index) in attachments.items" :key="index">
                <VEntity :is-selected="isChecked(attachment)">
                  <template #checkbox>
                    <input
                      :checked="selectedAttachments.has(attachment)"
                      class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                      type="checkbox"
                      @click="handleSelect(attachment)"
                    />
                  </template>
                  <template #start>
                    <VEntityField>
                      <template #description>
                        <div
                          class="h-10 w-10 rounded border bg-white p-1 hover:shadow-sm"
                        >
                          <AttachmentFileTypeIcon
                            :display-ext="false"
                            :file-name="attachment.spec.displayName"
                            :width="8"
                            :height="8"
                          />
                        </div>
                      </template>
                    </VEntityField>
                    <VEntityField
                      :title="attachment.spec.displayName"
                      @click="handleClickItem(attachment)"
                    >
                      <template #description>
                        <VSpace>
                          <span class="text-xs text-gray-500">
                            {{ attachment.spec.mediaType }}
                          </span>
                          <span class="text-xs text-gray-500">
                            {{ prettyBytes(attachment.spec.size || 0) }}
                          </span>
                        </VSpace>
                      </template>
                    </VEntityField>
                  </template>
                  <template #end>
                    <VEntityField
                      :description="
                        getPolicyName(attachment.spec.policyRef?.name)
                      "
                    />
                    <VEntityField>
                      <template #description>
                        <RouterLink
                          :to="{
                            name: 'UserDetail',
                            params: { name: attachment.spec.uploadedBy?.name },
                          }"
                          class="text-xs text-gray-500"
                        >
                          {{ attachment.spec.uploadedBy?.name }}
                        </RouterLink>
                      </template>
                    </VEntityField>
                    <VEntityField v-if="attachment.metadata.deletionTimestamp">
                      <template #description>
                        <VStatusDot
                          v-tooltip="`删除中`"
                          state="warning"
                          animate
                        />
                      </template>
                    </VEntityField>
                    <VEntityField
                      :description="
                        formatDatetime(attachment.metadata.creationTimestamp)
                      "
                    />
                  </template>
                  <template #dropdownItems>
                    <VButton
                      v-close-popper
                      block
                      type="danger"
                      @click="handleDelete(attachment)"
                    >
                      删除
                    </VButton>
                  </template>
                </VEntity>
              </li>
            </ul>
          </div>

          <template #footer>
            <div class="bg-white sm:flex sm:items-center sm:justify-end">
              <VPagination
                :page="attachments.page"
                :size="attachments.size"
                :total="attachments.total"
                @change="handlePaginationChange"
              />
            </div>
          </template>
        </VCard>
      </div>
    </div>
  </div>
</template>
