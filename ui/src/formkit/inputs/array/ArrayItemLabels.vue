<script setup lang="ts">
import { utils } from "@halo-dev/ui-shared";
import ColorLabel from "./labels/ColorLabel.vue";
import IconifyLabel from "./labels/IconifyLabel.vue";
import type { FormattedItemLabel } from "./types";

const props = withDefaults(
  defineProps<{
    labels?: FormattedItemLabel[];
    linkImages?: boolean;
  }>(),
  {
    labels: undefined,
    linkImages: true,
  }
);

const handleImageClick = (event: MouseEvent) => {
  if (props.linkImages !== false) {
    event.stopPropagation();
  }
};
</script>

<template>
  <template v-for="(itemLabel, labelIndex) in labels ?? []" :key="labelIndex">
    <component
      :is="linkImages === false ? 'span' : 'a'"
      v-if="itemLabel.type === 'image'"
      :href="linkImages === false ? undefined : itemLabel.value"
      :target="linkImages === false ? undefined : '_blank'"
      class="block aspect-1 size-8 flex-none"
      @click="handleImageClick"
    >
      <img
        v-tooltip="
          $t('core.formkit.array.image_tooltip', {
            value: itemLabel.value,
          })
        "
        :src="utils.attachment.getThumbnailUrl(itemLabel.value, 'S')"
        :alt="
          $t('core.formkit.array.image_tooltip', {
            value: itemLabel.value,
          })
        "
        class="size-full object-cover"
        @error="utils.attachment.handleThumbnailError($event, itemLabel.value)"
      />
    </component>
    <span v-if="itemLabel.type === 'text'">
      {{ itemLabel.value }}
    </span>
    <ColorLabel v-if="itemLabel.type === 'color'" :item-label="itemLabel" />
    <IconifyLabel v-if="itemLabel.type === 'iconify'" :item-label="itemLabel" />
  </template>
</template>
