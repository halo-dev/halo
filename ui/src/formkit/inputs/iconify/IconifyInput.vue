<script setup lang="ts">
import type { FormKitFrameworkContext } from "@formkit/core";
import { IconClose, VDropdown } from "@halo-dev/components";
import { Icon } from "@iconify/vue";
import { computed, type PropType } from "vue";
import IconifyPicker from "./IconifyPicker.vue";
import type { IconifyFormat } from "./types";

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const format = computed(() => props.context.format as IconifyFormat);

const onSelect = (icon: string) => {
  props.context.node.input(icon);
};
</script>

<template>
  <div class="inline-flex items-center gap-1.5">
    <VDropdown
      class="inline-flex"
      popper-class="w-full sm:w-auto"
      placement="bottom-start"
    >
      <button
        type="button"
        class="inline-flex h-9 items-center justify-center rounded-lg border bg-white px-2 transition-all hover:bg-gray-50 hover:shadow active:bg-gray-100"
        aria-label="Select icon"
      >
        <div v-if="!context._value" class="text-sm text-gray-600">
          {{ $t("core.formkit.iconify.placeholder") }}
        </div>
        <div
          v-else
          class="inline-flex size-full items-center justify-center [&>*]:size-5"
        >
          <img
            v-if="['url', 'dataurl'].includes(format)"
            :src="context._value"
            alt="Selected icon"
          />
          <Icon v-else-if="format === 'name'" :icon="context._value" />
          <div
            v-else
            class="inline-flex size-full items-center justify-center"
            v-html="context._value"
          ></div>
        </div>
      </button>
      <template #popper>
        <IconifyPicker :format="format" @select="onSelect" />
      </template>
    </VDropdown>
    <button
      v-if="context._value"
      type="button"
      aria-label="Clear selected icon"
      @click="context.node.input(undefined)"
    >
      <IconClose class="text-gray-500 hover:text-gray-900" />
    </button>
  </div>
</template>
