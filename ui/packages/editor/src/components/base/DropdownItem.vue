<script lang="ts" setup>
import MingcuteCheckCircleLine from "~icons/mingcute/check-circle-line";

const props = withDefaults(
  defineProps<{
    disabled?: boolean;
    isActive?: boolean;
  }>(),
  {
    disabled: false,
    isActive: false,
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
  <div
    role="menuitem"
    tabindex="-1"
    :class="[
      { 'cursor-not-allowed opacity-70': disabled },
      { 'hover:bg-gray-100': !disabled },
      { 'px-3': !$slots.icon },
      { 'px-1.5': !!$slots.icon },
    ]"
    class="group my-1.5 flex min-h-9 cursor-pointer flex-row items-center gap-3 rounded py-1 transition-colors first:mt-0 last:mb-0"
    @click="handleClick"
  >
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
        { '!font-medium !text-gray-900': isActive },
        { 'group-hover:font-medium group-hover:text-gray-900': !disabled },
      ]"
    >
      <slot />
    </div>

    <MingcuteCheckCircleLine
      v-if="isActive"
      class="size-4 flex-none text-gray-900"
    />
  </div>
</template>
