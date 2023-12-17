<script lang="ts" setup>
import type { ComputedRef } from "vue";
import { computed, provide, useSlots } from "vue";
import { VTabbar } from "./index";
import type { Direction, Type } from "./interface";

const props = withDefaults(
  defineProps<{
    activeId?: number | string;
    type?: Type;
    direction?: Direction;
    idKey?: string;
    labelKey?: string;
  }>(),
  {
    activeId: undefined,
    type: "default",
    direction: "row",
    idKey: "id",
    labelKey: "label",
  }
);

provide<ComputedRef<string | number | undefined>>(
  "activeId",
  computed(() => props.activeId)
);

const emit = defineEmits<{
  (event: "update:activeId", value: number | string): void;
  (event: "change", value: number | string): void;
}>();

const slots = useSlots();

const tabItems = computed(() => {
  return slots.default?.().map(({ props: slotProps }) => {
    return {
      id: slotProps?.[props.idKey],
      label: slotProps?.[props.labelKey],
    };
  });
});

const classes = computed(() => {
  return [`tabs-direction-${props.direction}`];
});

const handleChange = (id: string | number) => {
  emit("update:activeId", id);
  emit("change", id);
};
</script>
<template>
  <div :class="classes" class="tabs-wrapper">
    <div class="tabs-bar-wrapper">
      <VTabbar
        :active-id="activeId"
        :direction="direction"
        :items="tabItems"
        :type="type"
        @change="handleChange"
      />
    </div>
    <div class="tabs-items-wrapper">
      <slot />
    </div>
  </div>
</template>
<style lang="scss">
.tabs-wrapper {
  @apply flex;

  &.tabs-direction-row {
    @apply flex-col;

    .tabs-items-wrapper {
      @apply mt-2;
    }
  }

  &.tabs-direction-column {
    .tabs-items-wrapper {
      @apply ml-2;
    }
  }
}
</style>
