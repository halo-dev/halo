<script lang="ts" setup>
import AttachmentPermalinkList from "@/components/attachment/AttachmentPermalinkList.vue";
import LazyImage from "@/components/image/LazyImage.vue";
import { formatDatetime } from "@/utils/date";
import { isImage } from "@/utils/image";
import { coreApiClient } from "@halo-dev/api-client";
import {
  IconRiPencilFill,
  VButton,
  VDescription,
  VDescriptionItem,
  VLoading,
  VModal,
  VSpace,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import prettyBytes from "pretty-bytes";
import { computed, ref, toRefs, useTemplateRef } from "vue";
import DisplayNameEditForm from "./DisplayNameEditForm.vue";

const props = withDefaults(
  defineProps<{
    name?: string;
    mountToBody?: boolean;
  }>(),
  {
    name: undefined,
    mountToBody: false,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const { name } = toRefs(props);

const modal = useTemplateRef<InstanceType<typeof VModal> | null>("modal");
const onlyPreview = ref(false);

const { data: attachment, isLoading } = useQuery({
  queryKey: ["core:attachment-by-name", name],
  queryFn: async () => {
    const { data } = await coreApiClient.storage.attachment.getAttachment({
      name: name.value as string,
    });
    return data;
  },
  enabled: computed(() => !!name.value),
});

const policyName = computed(() => {
  return attachment.value?.spec.policyName;
});

const groupName = computed(() => {
  return attachment.value?.spec.groupName;
});

const { data: policy } = useQuery({
  queryKey: ["core:attachment-policy-by-name", policyName],
  queryFn: async () => {
    if (!policyName.value) {
      return;
    }

    const { data } = await coreApiClient.storage.policy.getPolicy({
      name: policyName.value,
    });

    return data;
  },
  enabled: computed(() => !!policyName.value),
});

const { data: group } = useQuery({
  queryKey: ["core:attachment-group-by-name", groupName],
  queryFn: async () => {
    if (!groupName.value) {
      return;
    }

    const { data } = await coreApiClient.storage.group.getGroup({
      name: groupName.value,
    });

    return data;
  },
  enabled: computed(() => !!groupName.value),
});

const showDisplayNameForm = ref(false);
</script>
<template>
  <VModal
    ref="modal"
    :title="
      $t('core.attachment.detail_modal.title', {
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
      <VLoading v-if="isLoading" />
      <div v-else class="overflow-hidden bg-white">
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
            >
            </VDescriptionItem>
            <VDescriptionItem
              :label="$t('core.attachment.detail_modal.fields.group')"
              :content="
                group?.spec.displayName ||
                $t('core.attachment.common.text.ungrouped')
              "
            />
            <VDescriptionItem
              :label="$t('core.attachment.detail_modal.fields.display_name')"
            >
              <DisplayNameEditForm
                v-if="showDisplayNameForm && attachment"
                :attachment="attachment"
                @close="showDisplayNameForm = false"
              />
              <div v-else class="flex items-center gap-3">
                <span>
                  {{ attachment?.spec.displayName }}
                </span>
                <HasPermission :permissions="['system:attachments:manage']">
                  <IconRiPencilFill
                    class="cursor-pointer text-sm text-gray-600 hover:text-gray-900"
                    @click="showDisplayNameForm = true"
                  />
                </HasPermission>
              </div>
            </VDescriptionItem>
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
