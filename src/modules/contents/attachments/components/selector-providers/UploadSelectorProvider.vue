<script lang="ts" setup>
import {
  VEmpty,
  IconCheckboxFill,
  VCard,
  IconDeleteBin,
  useDialog,
} from "@halo-dev/components";

import { apiClient, type AttachmentLike } from "@halo-dev/admin-shared";
import LazyImage from "@/components/image/LazyImage.vue";
import type { Attachment } from "@halo-dev/api-client";
import FilePondUpload from "@/components/upload/FilePondUpload.vue";
import AttachmentFileTypeIcon from "../AttachmentFileTypeIcon.vue";
import { computed, ref, watchEffect } from "vue";
import type { AxiosResponse } from "axios";
import { isImage } from "@/utils/image";
import { useFetchAttachmentPolicy } from "../../composables/use-attachment-policy";
import { useFetchAttachmentGroup } from "../../composables/use-attachment-group";

withDefaults(
  defineProps<{
    selected: AttachmentLike[];
  }>(),
  {
    selected: () => [],
  }
);

const emit = defineEmits<{
  (event: "update:selected", attachments: AttachmentLike[]): void;
}>();

const { policies } = useFetchAttachmentPolicy({ fetchOnMounted: true });
const policyMap = computed(() => {
  return [
    {
      label: "选择存储策略",
      value: "",
    },
    ...policies.value.map((policy) => {
      return {
        label: policy.spec.displayName,
        value: policy.metadata.name,
      };
    }),
  ];
});
const selectedPolicy = ref("");

const { groups } = useFetchAttachmentGroup({ fetchOnMounted: true });
const groupMap = computed(() => {
  return [
    {
      label: "选择分组",
      value: "",
    },
    ...groups.value.map((group) => {
      return {
        label: group.spec.displayName,
        value: group.metadata.name,
      };
    }),
  ];
});
const selectedGroup = ref("");

const attachments = ref<Set<Attachment>>(new Set<Attachment>());
const selectedAttachments = ref<Set<Attachment>>(new Set<Attachment>());

const uploadHandler = computed(() => {
  return (file, config) =>
    apiClient.extension.storage.attachment.uploadAttachment(
      {
        file,
        policyName: selectedPolicy.value,
        groupName: selectedGroup.value,
      },
      config
    );
});

const onUploaded = async (response: AxiosResponse) => {
  const attachment = response.data as Attachment;

  const { data } =
    await apiClient.extension.storage.attachment.getstorageHaloRunV1alpha1Attachment(
      {
        name: attachment.metadata.name,
      }
    );
  attachments.value.add(data);
  selectedAttachments.value.add(data);
};

const handleSelect = async (attachment: Attachment | undefined) => {
  if (!attachment) return;
  if (selectedAttachments.value.has(attachment)) {
    selectedAttachments.value.delete(attachment);
    return;
  }
  selectedAttachments.value.add(attachment);
};

const dialog = useDialog();

const handleDelete = async (attachment: Attachment) => {
  dialog.warning({
    title: "确定要删除当前的附件吗？",
    confirmType: "danger",
    onConfirm: async () => {
      try {
        await apiClient.extension.storage.attachment.deletestorageHaloRunV1alpha1Attachment(
          {
            name: attachment.metadata.name,
          }
        );
        attachments.value.delete(attachment);
        selectedAttachments.value.delete(attachment);
      } catch (e) {
        console.error("Failed to delete attachment", e);
      }
    },
  });
};

watchEffect(() => {
  emit("update:selected", Array.from(selectedAttachments.value));
});
</script>

<template>
  <div class="flex h-full flex-col gap-4 sm:flex-row">
    <div class="h-full w-full space-y-4 overflow-auto sm:w-96">
      <FormKit type="form">
        <FormKit
          v-model="selectedPolicy"
          type="select"
          :options="policyMap"
          label="存储策略"
        ></FormKit>
        <FormKit
          v-model="selectedGroup"
          :options="groupMap"
          type="select"
          label="分组"
        ></FormKit>
      </FormKit>
      <FilePondUpload
        ref="FilePondUploadRef"
        :allow-multiple="true"
        :handler="uploadHandler"
        :max-parallel-uploads="5"
        :disabled="!selectedPolicy"
        :label-idle="
          selectedPolicy ? '点击选择文件或者拖拽文件到此处' : '请先选择存储策略'
        "
        @uploaded="onUploaded"
      />
    </div>
    <div class="h-full flex-1 overflow-auto">
      <VEmpty
        v-if="!attachments.size"
        message="当前没有上传的文件，你可以点击左侧区域上传文件"
        title="当前没有上传的文件"
      >
      </VEmpty>
      <div
        v-else
        class="grid grid-cols-3 gap-x-2 gap-y-3 p-0.5 sm:grid-cols-3 md:grid-cols-4 xl:grid-cols-4 2xl:grid-cols-6"
        role="list"
      >
        <VCard
          v-for="(attachment, index) in Array.from(attachments)"
          :key="index"
          :body-class="['!p-0']"
          :class="{
            'ring-1 ring-primary': selectedAttachments.has(attachment),
          }"
          class="hover:shadow"
          @click="handleSelect(attachment)"
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
                class="pointer-events-none object-cover group-hover:opacity-75"
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
              class="pointer-events-none block truncate px-2 py-1 text-center text-xs font-medium text-gray-700"
            >
              {{ attachment.spec.displayName }}
            </p>

            <div
              :class="{ '!flex': selectedAttachments.has(attachment) }"
              class="absolute top-0 left-0 hidden h-1/3 w-full justify-end bg-gradient-to-b from-gray-300 to-transparent ease-in-out group-hover:flex"
            >
              <IconDeleteBin
                class="mt-1 mr-1 hidden h-5 w-5 cursor-pointer text-red-400 transition-all hover:text-red-600 group-hover:block"
                @click.stop="handleDelete(attachment)"
              />
              <IconCheckboxFill
                :class="{
                  '!text-primary': selectedAttachments.has(attachment),
                }"
                class="mt-1 mr-1 h-5 w-5 cursor-pointer text-white transition-all hover:text-primary"
              />
            </div>
          </div>
        </VCard>
      </div>
    </div>
  </div>
</template>
