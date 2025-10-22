<script lang="ts" setup>
import { postLabels } from "@/constants/labels";
import type { ListedPost } from "@halo-dev/api-client";
import { IconTimerLine, VEntityField } from "@halo-dev/components";
import { utils } from "@halo-dev/console-shared";

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
          v-tooltip="utils.date.format(post.post.spec.publishTime)"
          class="entity-field-description"
        >
          {{ utils.date.timeAgo(post.post.spec.publishTime) }}
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
