<script lang="ts" setup>
import {
  LocalThumbnailStatusPhaseEnum,
  type LocalThumbnail,
} from "@halo-dev/api-client";
import { VButton } from "@halo-dev/components";
import { toRefs } from "vue";
import { useThumbnailControl } from "../composables/use-thumbnail-control";
import {
  SIZE_MAP,
  useThumbnailDetail,
} from "../composables/use-thumbnail-detail";

const props = withDefaults(
  defineProps<{
    thumbnail: LocalThumbnail;
  }>(),
  {}
);

const { thumbnail } = toRefs(props);

const { phase } = useThumbnailDetail(thumbnail);
const { handleRetry } = useThumbnailControl(thumbnail);
</script>
<template>
  <li>
    <div
      class="flex w-full cursor-pointer items-center justify-between space-x-3 rounded border p-3 hover:border-primary"
    >
      <a
        :href="thumbnail.spec.thumbnailUri"
        target="_blank"
        class="block flex-none"
      >
        <img
          v-if="
            thumbnail.status.phase === LocalThumbnailStatusPhaseEnum.Succeeded
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
      <div class="flex min-w-0 flex-1 flex-col space-y-2 text-xs text-gray-900">
        <span class="font-semibold">
          {{ SIZE_MAP[thumbnail.spec.size] }}
        </span>
        <a
          :href="thumbnail.spec.thumbnailUri"
          target="_blank"
          class="line-clamp-1 hover:text-gray-600"
        >
          {{ thumbnail.spec.thumbnailUri }}
        </a>
      </div>
      <div class="flex flex-none items-center gap-2">
        <component
          :is="phase.icon"
          v-tooltip="$t(phase.label)"
          class="h-4.5 w-4.5"
          :class="phase.color"
        />
        <VButton
          v-if="
            thumbnail.status.phase !== LocalThumbnailStatusPhaseEnum.Succeeded
          "
          size="sm"
          @click="handleRetry"
        >
          {{ $t("core.common.buttons.retry") }}
        </VButton>
      </div>
    </div>
  </li>
</template>
