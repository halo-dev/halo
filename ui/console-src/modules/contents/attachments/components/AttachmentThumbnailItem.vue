<script lang="ts" setup>
import { formatDatetime } from "@/utils/date";
import {
  LocalThumbnailStatusPhaseEnum,
  type LocalThumbnail,
} from "@halo-dev/api-client";
import { VButton, VEntity, VEntityField } from "@halo-dev/components";
import { toRefs } from "vue";
import { useThumbnailControl } from "../composables/use-thumbnail-control";
import {
  SIZE_MAP,
  useThumbnailDetail,
} from "../composables/use-thumbnail-detail";

const props = defineProps<{
  thumbnail: LocalThumbnail;
}>();

const { thumbnail } = toRefs(props);

const { phase } = useThumbnailDetail(thumbnail);
const { handleRetry } = useThumbnailControl(thumbnail);
</script>
<template>
  <VEntity>
    <template #start>
      <VEntityField>
        <template #description>
          <a
            :href="thumbnail.spec.thumbnailUri"
            target="_blank"
            class="block flex-none"
          >
            <img
              v-if="
                thumbnail.status.phase ===
                LocalThumbnailStatusPhaseEnum.Succeeded
              "
              :src="thumbnail.spec.thumbnailUri"
              alt=""
              class="h-10 w-10 rounded-md object-cover"
              loading="lazy"
            />
            <div
              v-else
              class="flex h-10 w-10 items-center justify-center rounded-md border"
            >
              <component
                :is="phase.icon"
                class="h-4.5 w-4.5"
                :class="phase.color"
              />
            </div>
          </a>
        </template>
      </VEntityField>
      <VEntityField :title="SIZE_MAP[thumbnail.spec.size]">
        <template #description>
          <a
            :href="thumbnail.spec.thumbnailUri"
            target="_blank"
            class="truncate text-xs text-gray-500 hover:text-gray-900"
          >
            {{ thumbnail.spec.thumbnailUri }}
          </a>
        </template>
      </VEntityField>
    </template>
    <template #end>
      <VEntityField
        :description="formatDatetime(thumbnail.metadata.creationTimestamp)"
      />
      <VEntityField>
        <template #description>
          <component
            :is="phase.icon"
            v-tooltip="$t(phase.label)"
            class="h-4.5 w-4.5"
            :class="phase.color"
          />
        </template>
      </VEntityField>
      <VEntityField
        v-if="
          thumbnail.status.phase !== LocalThumbnailStatusPhaseEnum.Succeeded
        "
      >
        <template #description>
          <VButton size="sm" @click="handleRetry">
            {{ $t("core.common.buttons.retry") }}
          </VButton>
        </template>
      </VEntityField>
    </template>
  </VEntity>
</template>
