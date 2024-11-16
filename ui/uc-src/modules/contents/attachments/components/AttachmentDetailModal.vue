<script lang="ts" setup>
import AttachmentPermalinkList from "@/components/attachment/AttachmentPermalinkList.vue";
import LazyImage from "@/components/image/LazyImage.vue";
import { formatDatetime } from "@/utils/date";
import { isImage } from "@/utils/image";
import { type Attachment } from "@halo-dev/api-client";
import {
  VButton,
  VDescription,
  VDescriptionItem,
  VModal,
  VSpace,
} from "@halo-dev/components";
import prettyBytes from "pretty-bytes";
import { ref, useTemplateRef } from "vue";

withDefaults(
  defineProps<{
    attachment: Attachment;
    mountToBody?: boolean;
  }>(),
  {
    mountToBody: false,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = useTemplateRef<InstanceType<typeof VModal> | null>("modal");
const onlyPreview = ref(false);
</script>
<template>
  <VModal
    ref="modal"
    :title="
      $t('core.uc_attachment.detail_modal.title', {
        display_name: attachment?.spec.displayName || '',
      })
    "
    :width="1000"
    :mount-to-body="mountToBody"
    :layer-closable="true"
    height="calc(100vh - 20px)"
    :body-class="['!p-0']"
    @close="emit('close')"
  >
    <template #actions>
      <slot name="actions"></slot>
    </template>
    <div>
      <div class="overflow-hidden bg-white">
        <div
          v-if="onlyPreview && isImage(attachment?.spec.mediaType)"
          class="flex justify-center p-4"
        >
          <img
            v-tooltip.bottom="
              $t('core.uc_attachment.detail_modal.preview.click_to_exit')
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
              :label="$t('core.uc_attachment.detail_modal.fields.preview')"
            >
              <div
                v-if="isImage(attachment?.spec.mediaType)"
                @click="onlyPreview = !onlyPreview"
              >
                <LazyImage
                  :alt="attachment?.spec.displayName"
                  :src="
                    attachment?.status?.thumbnails?.M ||
                    attachment?.status?.permalink
                  "
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
                    $t(
                      "core.uc_attachment.detail_modal.preview.video_not_support"
                    )
                  }}
                </video>
              </div>
              <div v-else-if="attachment?.spec.mediaType?.startsWith('audio/')">
                <audio :src="attachment.status?.permalink" controls>
                  {{
                    $t(
                      "core.uc_attachment.detail_modal.preview.audio_not_support"
                    )
                  }}
                </audio>
              </div>
              <span v-else>
                {{ $t("core.uc_attachment.detail_modal.preview.not_support") }}
              </span>
            </VDescriptionItem>
            <VDescriptionItem
              :label="$t('core.uc_attachment.detail_modal.fields.display_name')"
            >
              <span>
                {{ attachment?.spec.displayName }}
              </span>
            </VDescriptionItem>
            <VDescriptionItem
              :label="$t('core.uc_attachment.detail_modal.fields.media_type')"
              :content="attachment?.spec.mediaType"
            />
            <VDescriptionItem
              :label="$t('core.uc_attachment.detail_modal.fields.size')"
              :content="prettyBytes(attachment?.spec.size || 0)"
            />
            <VDescriptionItem
              :label="$t('core.uc_attachment.detail_modal.fields.owner')"
              :content="attachment?.spec.ownerName"
            />
            <VDescriptionItem
              :label="
                $t('core.uc_attachment.detail_modal.fields.creation_time')
              "
              :content="formatDatetime(attachment?.metadata.creationTimestamp)"
            />
            <VDescriptionItem
              :label="$t('core.uc_attachment.detail_modal.fields.permalink')"
            >
              <AttachmentPermalinkList :attachment="attachment" />
            </VDescriptionItem>
          </VDescription>
        </div>
      </div>
    </div>

    <template #footer>
      <VSpace>
        <VButton type="default" @click="modal?.close()">
          {{ $t("core.common.buttons.close_and_shortcut") }}
        </VButton>
        <slot name="footer" />
      </VSpace>
    </template>
  </VModal>
</template>
