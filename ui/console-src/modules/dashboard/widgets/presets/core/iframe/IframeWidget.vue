<script setup lang="ts">
import WidgetCard from "@console/modules/dashboard/components/WidgetCard.vue";

defineProps<{
  previewMode?: boolean;
  editMode?: boolean;
  config: {
    title?: string;
    url?: string;
  };
}>();
</script>
<template>
  <WidgetCard
    :title="
      previewMode
        ? $t('core.dashboard.widgets.presets.iframe.title')
        : config.title
    "
  >
    <div
      v-if="!config.url"
      class="flex h-full w-full items-center justify-center"
    >
      <span class="text-sm text-gray-600">
        {{ $t("core.dashboard.widgets.presets.iframe.empty.title") }}
      </span>
    </div>
    <iframe
      v-else
      :src="config.url"
      sandbox="allow-scripts allow-same-origin"
      credentialless
      referrerpolicy="no-referrer"
      class="h-full w-full border-none"
      :class="{
        'pointer-events-none': editMode,
      }"
    ></iframe>
  </WidgetCard>
</template>
