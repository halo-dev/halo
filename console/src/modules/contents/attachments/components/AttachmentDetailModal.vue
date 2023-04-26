<script lang="ts" setup>
import {
  VButton,
  VDescription,
  VDescriptionItem,
  VModal,
  VSpace,
} from "@halo-dev/components";
import LazyImage from "@/components/image/LazyImage.vue";
import type { Attachment } from "@halo-dev/api-client";
import prettyBytes from "pretty-bytes";
import { computed, ref } from "vue";
import { apiClient } from "@/utils/api-client";
import { isImage } from "@/utils/image";
import { formatDatetime } from "@/utils/date";
import { useFetchAttachmentGroup } from "../composables/use-attachment-group";
import { useQuery } from "@tanstack/vue-query";
import AttachmentPermalinkList from "./AttachmentPermalinkList.vue";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    attachment: Attachment | undefined;
    mountToBody?: boolean;
  }>(),
  {
    visible: false,
    attachment: undefined,
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

    setTimeout(() => {
      emit("close");
    }, 200);
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
    :body-class="['!p-0']"
    @update:visible="onVisibleChange"
  >
    <template #actions>
      <slot name="actions"></slot>
    </template>
    <div class="overflow-hidden bg-white">
      <div
        v-if="onlyPreview && isImage(attachment?.spec.mediaType)"
        class="flex justify-center p-4"
      >
        <img
          v-tooltip.bottom="
            $t('core.attachment.detail_modal.preview.click_to_exit')
          "
          :alt="attachment?.spec.displayName"
          :src="attachment?.status?.permalink"
          class="w-auto transform-gpu cursor-pointer rounded"
          @click="onlyPreview = !onlyPreview"
        />
      </div>
      <div v-else>
        <VDescription>
          <VDescriptionItem
            :label="$t('core.attachment.detail_modal.fields.preview')"
          >
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
          </VDescriptionItem>
          <VDescriptionItem
            :label="$t('core.attachment.detail_modal.fields.storage_policy')"
            :content="policy?.spec.displayName"
          ></VDescriptionItem>
          <VDescriptionItem
            :label="$t('core.attachment.detail_modal.fields.group')"
            :content="
              getGroupName(attachment?.spec.groupName) ||
              $t('core.attachment.common.text.ungrouped')
            "
          />
          <VDescriptionItem
            :label="$t('core.attachment.detail_modal.fields.display_name')"
            :content="attachment?.spec.displayName"
          />
          <VDescriptionItem
            :label="$t('core.attachment.detail_modal.fields.media_type')"
            :content="attachment?.spec.mediaType"
          />
          <VDescriptionItem
            :label="$t('core.attachment.detail_modal.fields.size')"
            :content="prettyBytes(attachment?.spec.size || 0)"
          />
          <VDescriptionItem
            :label="$t('core.attachment.detail_modal.fields.owner')"
            :content="attachment?.spec.ownerName"
          />
          <VDescriptionItem
            :label="$t('core.attachment.detail_modal.fields.creation_time')"
            :content="formatDatetime(attachment?.metadata.creationTimestamp)"
          />
          <VDescriptionItem
            :label="$t('core.attachment.detail_modal.fields.permalink')"
          >
            <AttachmentPermalinkList :attachment="attachment" />
          </VDescriptionItem>
        </VDescription>
      </div>
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
