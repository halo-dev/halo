<script lang="ts" setup>
import { computed, provide, useSlots } from "vue";
import type { PropType, ComputedRef } from "vue";
import { VTabbar } from "./index";
import type { Type } from "@/components/base/tabs/interface";

const props = defineProps({
  activeId: {
    type: [Number, String],
  },
  type: {
    type: String as PropType<Type>,
    default: "default",
  },
  idKey: {
    type: String,
    default: "id",
  },
  labelKey: {
    type: String,
    default: "label",
  },
});

provide<ComputedRef<string | number | undefined>>(
  "activeId",
  computed(() => props.activeId)
);

const emit = defineEmits(["update:activeId", "change"]);

const slots = useSlots();

const tabItems = computed(() => {
  return slots.default?.().map(({ props: slotProps }) => {
    return {
      id: slotProps?.[props.idKey],
      label: slotProps?.[props.labelKey],
    };
  });
});

const handleChange = (id: string | number) => {
  emit("update:activeId", id);
  emit("change", id);
};
</script>
<template>
  <div class="tabs-wrapper">
    <div class="tabs-bar-wrapper">
      <VTabbar
        :activeId="activeId"
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
<style lang="scss"></style>
