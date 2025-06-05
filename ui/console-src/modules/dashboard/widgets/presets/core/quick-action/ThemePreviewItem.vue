<script lang="ts" setup>
import ThemePreviewModal from "@console/modules/interface/themes/components/preview/ThemePreviewModal.vue";
import { ref } from "vue";
import QuickActionItem from "./QuickActionItem.vue";
import type { QuickActionItemDefinition } from "./types";

defineProps<{
  item: QuickActionItemDefinition;
}>();

const visible = ref(false);
</script>

<template>
  <QuickActionItem
    :item="{
      ...item,
      action: () => {
        visible = true;
      },
    }"
  />

  <ThemePreviewModal
    v-if="visible"
    :title="
      $t('core.dashboard.widgets.presets.quickaction.actions.view_site.title')
    "
    @close="visible = false"
  />
</template>
