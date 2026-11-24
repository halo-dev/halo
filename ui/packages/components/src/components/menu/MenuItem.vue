<script lang="ts" setup>
import { inject, ref, useSlots, watch } from "vue";
import { IconArrowRight } from "../../icons/icons";
import { menuContextKey } from "./context";

const props = withDefaults(
  defineProps<{
    id?: string;
    title?: string;
    active?: boolean;
  }>(),
  {
    id: "",
    title: "",
    active: false,
  }
);

const emit = defineEmits<{
  (event: "select", id: string): void;
}>();

const slots = useSlots();

const open = ref(false);

const menu = inject(menuContextKey, undefined);

watch(
  () => menu?.openIds.value?.includes(props.id) ?? false,
  (matched) => {
    if (matched) {
      open.value = true;
    }
  },
  { immediate: true }
);

function handleClick() {
  if (!open.value) {
    handleExpand();
  }
  emit("select", props.id);
}

function handleExpand() {
  if (slots.default) {
    open.value = !open.value;
  }
}
</script>

<template>
  <li
    :aria-current="active && !$slots.default ? 'page' : undefined"
    :aria-expanded="$slots.default ? open : undefined"
    :class="{ 'has-submenus': !!$slots.default }"
    class="menu-item group"
    tabindex="0"
    @click.stop="handleClick"
    @keydown.enter.stop.prevent="handleClick"
    @keydown.space.stop.prevent="handleClick"
  >
    <div :class="{ active }" class="menu-item-title">
      <span v-if="$slots.icon" class="menu-icon mr-2.5 self-center">
        <slot name="icon" />
      </span>
      <span
        :title="title || undefined"
        class="menu-title flex-1 self-center truncate"
      >
        {{ title }}
      </span>
      <span
        v-if="$slots.default"
        :class="{ open }"
        class="menu-icon-collapse self-center transition-all"
        @click.stop="handleExpand"
      >
        <IconArrowRight aria-hidden="true" />
      </span>
    </div>

    <Transition name="submenus-show">
      <ul v-show="$slots.default && open" class="sub-menu-items">
        <slot />
      </ul>
    </Transition>
  </li>
</template>

<style lang="scss">
.menu-item {
  @apply cursor-pointer;
}

.menu-item-title {
  @apply relative flex select-none rounded-base px-2 py-[0.4rem] text-base font-normal transition-all;

  &:hover,
  &.active {
    @apply bg-gray-100 font-medium;
  }

  &.active::after {
    @apply absolute;
    top: calc(50% - 13px);
    left: -8px;
    width: 3px;
    height: 26px;
    content: "";
    background: theme("colors.secondary");
    border-radius: 6px;
  }
}

.menu-icon-collapse {
  @apply rounded-full p-0.5 group-hover:bg-gray-200;

  &.open {
    @apply bg-gray-200;
    transform: rotate(90deg);
  }
}

.submenus-show-enter-active,
.submenus-show-leave-active {
  transition: opacity 0.1s ease;
}

.submenus-show-enter-from,
.submenus-show-leave-to {
  opacity: 0;
}

.submenus-show-enter-to,
.submenus-show-leave-from {
  opacity: 1;
}

@media (prefers-reduced-motion: reduce) {
  .submenus-show-enter-active,
  .submenus-show-leave-active {
    transition: none;
  }
}

.sub-menu-items {
  @apply my-1;

  // Align child labels with the parent label text: px-2 + icon (1.2em) + mr-2.5,
  // minus this row's own p-1.5.
  padding-left: calc(0.5rem + 1.2rem + 0.625rem - 0.375rem);

  .menu-item-title {
    @apply p-1.5 text-sm;
  }
}
</style>
