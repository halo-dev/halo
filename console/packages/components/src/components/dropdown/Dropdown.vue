<script lang="ts" setup>
import { Dropdown as FloatingDropdown, type Placement } from "floating-vue";
import "floating-vue/dist/style.css";
import { provide, ref } from "vue";
import { DropdownContextInjectionKey } from "./symbols";

withDefaults(
  defineProps<{
    placement?: Placement;
    triggers?: string[];
    classes?: string[];
  }>(),
  {
    placement: "bottom",
    triggers: () => ["click"],
    classes: () => [],
  }
);

const dropdownRef = ref();

function hide() {
  dropdownRef.value?.hide();
}

provide(DropdownContextInjectionKey, {
  hide,
});

const emit = defineEmits<{
  (event: "show"): void;
}>();

defineExpose({
  hide,
});
</script>

<template>
  <FloatingDropdown
    ref="dropdownRef"
    :placement="placement"
    :triggers="triggers"
    @show="emit('show')"
  >
    <slot />
    <template #popper>
      <div
        class="min-w-[13rem] p-1.5"
        :class="classes"
        role="menu"
        aria-orientation="vertical"
        aria-labelledby="menu-button"
        tabindex="-1"
      >
        <slot name="popper" />
      </div>
    </template>
  </FloatingDropdown>
</template>
