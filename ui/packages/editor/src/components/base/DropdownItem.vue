<script lang="ts" setup>
import MingcuteCheckCircleLine from "~icons/mingcute/check-circle-line";
import MingcuteCheckLine from "~icons/mingcute/check-line";

const props = withDefaults(
  defineProps<{
    disabled?: boolean;
    isActive?: boolean;
    selectionIndicator?: "leading" | "trailing";
  }>(),
  {
    disabled: false,
    isActive: false,
    selectionIndicator: "trailing",
  }
);

const emit = defineEmits<{
  (event: "click", e: MouseEvent): void;
}>();

function handleClick(e: MouseEvent) {
  if (props.disabled) {
    return;
  }
  emit("click", e);
}
</script>
<template>
  <button
    type="button"
    role="menuitem"
    tabindex="-1"
    :disabled="disabled"
    :aria-disabled="disabled"
    :data-state="isActive ? 'on' : 'off'"
    :data-selection-indicator="selectionIndicator"
    :class="[
      { 'cursor-not-allowed opacity-70': disabled },
      { 'hover:bg-gray-100': !disabled },
      { 'px-3': !$slots.icon },
      { 'px-1.5': !!$slots.icon },
    ]"
    class="dropdown-item group my-1.5 flex min-h-9 w-full cursor-pointer flex-row items-center gap-3 rounded py-1 text-left transition-colors first:mt-0 last:mb-0 focus-visible:bg-gray-100 focus-visible:outline-none"
    @click="handleClick"
  >
    <MingcuteCheckLine
      v-if="selectionIndicator === 'leading'"
      aria-hidden="true"
      class="dropdown-item__leading-indicator size-4 flex-none text-gray-900"
      :class="{ invisible: !isActive }"
    />

    <div
      v-if="$slots.icon"
      class="size-7 flex-none rounded bg-gray-100 p-1.5 [&>svg]:size-full"
      :class="{
        'group-hover:bg-white': !disabled,
      }"
    >
      <slot name="icon" />
    </div>

    <div
      class="min-w-0 flex-1 shrink text-sm text-gray-600"
      :class="[
        {
          '!font-medium !text-gray-900':
            isActive && selectionIndicator === 'trailing',
        },
        { '!text-gray-900': isActive && selectionIndicator === 'leading' },
        { 'group-hover:font-medium group-hover:text-gray-900': !disabled },
      ]"
    >
      <slot />
    </div>

    <slot name="suffix" />

    <MingcuteCheckCircleLine
      v-if="isActive && selectionIndicator === 'trailing'"
      class="size-4 flex-none text-gray-900"
    />
  </button>
</template>

<style>
.dropdown-item__leading-indicator {
  display: none;
}

:has(> .dropdown-item[data-selection-indicator="leading"][data-state="on"])
  > .dropdown-item[data-selection-indicator="leading"]
  > .dropdown-item__leading-indicator {
  display: block;
}
</style>
