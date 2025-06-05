<script lang="ts" setup>
import ThemePreviewModal from "@console/modules/interface/themes/components/preview/ThemePreviewModal.vue";
import { ref } from "vue";
import QuickLinkItem from "./QuickLinkItem.vue";
import type { QuickLinkItemDefinition } from "./types";

defineProps<{
  item: QuickLinkItemDefinition;
}>();

const visible = ref(false);
</script>

<template>
  <QuickLinkItem
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
      $t('core.dashboard.widgets.presets.quicklink.actions.view_site.title')
    "
    @close="visible = false"
  />
</template>
