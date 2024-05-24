<script lang="ts" setup>
import { postLabels } from "@/constants/labels";
import { formatDatetime } from "@/utils/date";
import { IconTimerLine, VEntityField } from "@halo-dev/components";
import type { ListedPost } from "packages/api-client/dist";

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
        <span class="entity-field-description">
          {{ formatDatetime(post.post.spec.publishTime) }}
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
