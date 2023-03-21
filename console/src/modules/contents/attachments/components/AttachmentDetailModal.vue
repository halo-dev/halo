<script lang="ts" setup>
import { VButton, VModal, VSpace } from "@halo-dev/components";
import LazyImage from "@/components/image/LazyImage.vue";
import type { Attachment } from "@halo-dev/api-client";
import prettyBytes from "pretty-bytes";
import { computed, ref } from "vue";
import { apiClient } from "@/utils/api-client";
import { isImage } from "@/utils/image";
import { formatDatetime } from "@/utils/date";
import { useFetchAttachmentGroup } from "../composables/use-attachment-group";
import { useQuery } from "@tanstack/vue-query";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    attachment: Attachment | null;
    mountToBody?: boolean;
  }>(),
  {
    visible: false,
    attachment: null,
    mountToBody: false,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const { groups } = useFetchAttachmentGroup();

const onlyPreview = ref(false);

const policyName = computed(() => {
  return props.attachment?.spec.policyName;
});

const { data: policy } = useQuery({
  queryKey: ["attachment-policy", policyName],
  queryFn: async () => {
    if (!policyName.value) {
      return;
    }

    const { data } =
      await apiClient.extension.storage.policy.getstorageHaloRunV1alpha1Policy({
        name: policyName.value,
      });

    return data;
  },
  refetchOnWindowFocus: false,
  enabled: computed(() => !!policyName.value),
});

const getGroupName = (name: string | undefined) => {
  const group = groups.value?.find((group) => group.metadata.name === name);
  return group?.spec.displayName || name;
};

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    onlyPreview.value = false;
    emit("close");
  }
};
</script>
<template>
  <VModal
    :title="
      $t('core.attachment.detail_modal.title', {
        display_name: attachment?.spec.displayName || '',
      })
    "
    :visible="visible"
    :width="1000"
    :mount-to-body="mountToBody"
    :layer-closable="true"
    height="calc(100vh - 20px)"
    @update:visible="onVisibleChange"
  >
    <template #actions>
      <slot name="actions"></slot>
    </template>
    <div class="overflow-hidden bg-white">
      <div
        v-if="onlyPreview && isImage(attachment?.spec.mediaType)"
        class="flex justify-center"
      >
        <img
          v-tooltip.bottom="
            $t('core.attachment.detail_modal.preview.click_to_exit')
          "
          :alt="attachment?.spec.displayName"
          :src="attachment?.status?.permalink"
          class="w-auto cursor-pointer rounded"
          @click="onlyPreview = !onlyPreview"
        />
      </div>
      <dl v-else>
        <div
          class="bg-gray-50 px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6"
        >
          <dt class="text-sm font-medium text-gray-900">
            {{ $t("core.attachment.detail_modal.fields.preview") }}
          </dt>
          <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
            <div
              v-if="isImage(attachment?.spec.mediaType)"
              @click="onlyPreview = !onlyPreview"
            >
              <LazyImage
                :alt="attachment?.spec.displayName"
                :src="attachment?.status?.permalink"
                classes="max-w-full cursor-pointer rounded sm:max-w-[50%]"
              >
                <template #loading>
                  <span class="text-gray-400">
                    {{ $t("core.common.status.loading") }}...
                  </span>
                </template>
                <template #error>
                  <span class="text-red-400">
                    {{ $t("core.common.status.loading_error") }}
                  </span>
                </template>
              </LazyImage>
            </div>
            <div v-else-if="attachment?.spec.mediaType?.startsWith('video/')">
              <video
                :src="attachment.status?.permalink"
                controls
                class="max-w-full rounded sm:max-w-[50%]"
              >
                {{
                  $t("core.attachment.detail_modal.preview.video_not_support")
                }}
              </video>
            </div>
            <div v-else-if="attachment?.spec.mediaType?.startsWith('audio/')">
              <audio :src="attachment.status?.permalink" controls>
                {{
                  $t("core.attachment.detail_modal.preview.audio_not_support")
                }}
              </audio>
            </div>
            <span v-else>
              {{ $t("core.attachment.detail_modal.preview.not_support") }}
            </span>
          </dd>
        </div>
        <div class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
          <dt class="text-sm font-medium text-gray-900">
            {{ $t("core.attachment.detail_modal.fields.storage_policy") }}
          </dt>
          <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
            {{ policy?.spec.displayName }}
          </dd>
        </div>
        <div class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
          <dt class="text-sm font-medium text-gray-900">
            {{ $t("core.attachment.detail_modal.fields.group") }}
          </dt>
          <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
            {{
              getGroupName(attachment?.spec.groupName) ||
              $t("core.attachment.common.text.ungrouped")
            }}
          </dd>
        </div>
        <div class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
          <dt class="text-sm font-medium text-gray-900">
            {{ $t("core.attachment.detail_modal.fields.display_name") }}
          </dt>
          <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
            {{ attachment?.spec.displayName }}
          </dd>
        </div>
        <div class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
          <dt class="text-sm font-medium text-gray-900">
            {{ $t("core.attachment.detail_modal.fields.media_type") }}
          </dt>
          <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
            {{ attachment?.spec.mediaType }}
          </dd>
        </div>
        <div class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
          <dt class="text-sm font-medium text-gray-900">
            {{ $t("core.attachment.detail_modal.fields.size") }}
          </dt>
          <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
            {{ prettyBytes(attachment?.spec.size || 0) }}
          </dd>
        </div>
        <div class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
          <dt class="text-sm font-medium text-gray-900">
            {{ $t("core.attachment.detail_modal.fields.owner") }}
          </dt>
          <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
            {{ attachment?.spec.ownerName }}
          </dd>
        </div>
        <div class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
          <dt class="text-sm font-medium text-gray-900">
            {{ $t("core.attachment.detail_modal.fields.creation_time") }}
          </dt>
          <dd class="mt-1 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
            {{ formatDatetime(attachment?.metadata.creationTimestamp) }}
          </dd>
        </div>
        <div class="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
          <dt class="text-sm font-medium text-gray-900">
            {{ $t("core.attachment.detail_modal.fields.permalink") }}
          </dt>
          <dd
            class="mt-1 text-sm text-gray-900 hover:text-blue-600 sm:col-span-2 sm:mt-0"
          >
            <a target="_blank" :href="attachment?.status?.permalink">
              {{ attachment?.status?.permalink }}
            </a>
          </dd>
        </div>
      </dl>
    </div>
    <template #footer>
      <VSpace>
        <VButton type="default" @click="onVisibleChange(false)">
          {{ $t("core.common.buttons.close_and_shortcut") }}
        </VButton>
        <slot name="footer" />
      </VSpace>
    </template>
  </VModal>
</template>
