<script lang="ts" setup>
import { IconArrowRight } from "../../icons/icons";
import { computed, inject, ref, useSlots } from "vue";

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

const openIds = inject<string[] | undefined>("openIds");

if (openIds?.includes(props.id)) {
  open.value = true;
}

const hasSubmenus = computed(() => {
  return slots.default && slots.default().length > 0;
});

function handleClick() {
  if (hasSubmenus.value) {
    open.value = !open.value;
    return;
  }
  emit("select", props.id);
}
</script>

<template>
  <li
    :class="{ 'has-submenus': hasSubmenus }"
    class="menu-item"
    @click.stop="handleClick"
  >
    <div :class="{ active }" class="menu-item-title">
      <span v-if="$slots.icon" class="menu-icon mr-3 self-center">
        <slot name="icon" />
      </span>
      <span class="menu-title flex-1 self-center">
        {{ title }}
      </span>
      <span
        v-if="$slots.default"
        :class="{ open }"
        class="menu-icon-collapse self-center transition-all"
      >
        <IconArrowRight />
      </span>
    </div>

    <Transition name="submenus-show">
      <ul v-show="$slots.default && open" class="sub-menu-items transition-all">
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
  @apply transition-all
  text-base
  flex
  select-none
  relative
  p-2
  font-normal
  rounded-base;

  &:hover,
  &.active {
    @apply bg-gray-100
    font-medium;
  }

  &.active::after {
    @apply absolute;
    top: calc(50% - 13px);
    left: -8px;
    width: 3px;
    height: 26px;
    content: "";
    background: #242e41;
    border-radius: 6px;
  }
}

.menu-icon-collapse.open {
  transform: rotate(90deg);
}

.submenus-show-enter-active {
  transition: all 0.1s ease-out;
}

.submenus-show-leave-active {
  transition: all 0.8s cubic-bezier(1, 0.5, 0.8, 1);
}

.submenus-show-enter-from,
.submenus-show-enter-to {
  transform: translateY(-10px);
  opacity: 0;
}
</style>
