<script lang="ts" setup>
import { postLabels } from "@/constants/labels";
import { formatDatetime, relativeTimeTo } from "@/utils/date";
import type { ListedPost } from "@halo-dev/api-client";
import { IconTimerLine, VEntityField } from "@halo-dev/components";

withDefaults(
  defineProps<{
    post: ListedPost;
  }>(),
  {}
);
</script>

<template>
  <VEntityField>
    <template #description>
      <div class="inline-flex items-center space-x-2">
        <span
          v-tooltip="formatDatetime(post.post.spec.publishTime)"
          class="entity-field-description"
        >
          {{ relativeTimeTo(post.post.spec.publishTime) }}
        </span>
        <IconTimerLine
          v-if="
            post.post.metadata.labels?.[postLabels.SCHEDULING_PUBLISH] ===
            'true'
          "
          v-tooltip="$t('core.post.list.fields.schedule_publish.tooltip')"
          class="text-sm"
        />
      </div>
    </template>
  </VEntityField>
</template>
