<script lang="ts" setup>
import { computed, inject, ref, useSlots } from "vue";
import { IconArrowRight } from "../../icons/icons";

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
  if (!open.value) {
    handleExpand();
  }
  emit("select", props.id);
}

function handleExpand() {
  if (hasSubmenus.value) {
    open.value = !open.value;
  }
}
</script>

<template>
  <li
    :class="{ 'has-submenus': hasSubmenus }"
    class="menu-item group"
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
        @click.stop="handleExpand"
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
  px-2
  py-[0.4rem]
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

.menu-icon-collapse {
  @apply group-hover:bg-gray-200 p-0.5 rounded-full;

  &.open {
    @apply bg-gray-200;
    transform: rotate(90deg);
  }
}

.submenus-show-enter-active,
.submenus-show-leave-active {
  transition: all 0.1s ease;
}

.submenus-show-enter-from,
.submenus-show-enter-to {
  opacity: 0;
}

.sub-menu-items {
  @apply pl-5 my-1;

  .menu-item-title {
    @apply p-1.5 text-sm;
  }
}
</style>
