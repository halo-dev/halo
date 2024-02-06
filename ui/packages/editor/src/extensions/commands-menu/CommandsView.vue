<script lang="ts" setup>
import { type PropType, ref, watch } from "vue";
import type { CommandMenuItem } from "@/types";
import { i18n } from "@/locales";
import scrollIntoView from "scroll-into-view-if-needed";

const props = defineProps({
  items: {
    type: Array as PropType<CommandMenuItem[]>,
    required: true,
  },

  command: {
    type: Function as PropType<(item: CommandMenuItem) => void>,
    required: true,
  },
});

const selectedIndex = ref(0);

watch(
  () => props.items,
  () => {
    selectedIndex.value = 0;
  }
);

function onKeyDown({ event }: { event: KeyboardEvent }) {
  if (event.key === "ArrowUp" || (event.key === "k" && event.ctrlKey)) {
    handleKeyUp();
    return true;
  }

  if (event.key === "ArrowDown" || (event.key === "j" && event.ctrlKey)) {
    handleKeyDown();
    return true;
  }

  if (event.key === "Enter") {
    handleKeyEnter();
    return true;
  }

  return false;
}

function handleKeyUp() {
  selectedIndex.value =
    (selectedIndex.value + props.items.length - 1) % props.items.length;
}

function handleKeyDown() {
  selectedIndex.value = (selectedIndex.value + 1) % props.items.length;
}

function handleKeyEnter() {
  handleSelectItem(selectedIndex.value);
}

function handleSelectItem(index: number) {
  const item = props.items[index];

  if (item) {
    props.command(item);
  }
}

watch(
  () => selectedIndex.value,
  () => {
    const selected = document.getElementById(
      `command-item-${selectedIndex.value}`
    );

    if (selected) {
      scrollIntoView(selected, { behavior: "smooth", scrollMode: "if-needed" });
    }
  }
);

defineExpose({
  onKeyDown,
});
</script>
<template>
  <div class="command-items">
    <template v-if="items.length">
      <div
        v-for="(item, index) in items"
        :id="`command-item-${index}`"
        :key="index"
        :class="{ 'is-selected': index === selectedIndex }"
        class="command-item group hover:bg-gray-100"
        @click="handleSelectItem(index)"
      >
        <component :is="item.icon" class="command-icon group-hover:!bg-white" />
        <span
          class="command-title group-hover:text-gray-900 group-hover:font-medium"
        >
          {{ i18n.global.t(item.title) }}
        </span>
      </div>
    </template>
    <div v-else class="command-empty">
      <span>
        {{ i18n.global.t("editor.extensions.commands_menu.no_results") }}
      </span>
    </div>
  </div>
</template>
<style lang="scss">
.command-items {
  @apply relative
  rounded-md
  bg-white
  overflow-hidden
  drop-shadow
  w-52
  p-1
  max-h-72
  overflow-y-auto;

  .command-item {
    @apply flex flex-row items-center rounded gap-4 p-1;

    &.is-selected {
      @apply bg-gray-100;

      .command-icon {
        @apply bg-white;
      }

      .command-title {
        @apply text-gray-900 font-medium;
      }
    }

    .command-icon {
      @apply bg-gray-100 p-1 rounded w-6 h-6;
    }

    .command-title {
      @apply text-xs 
      text-gray-600;
    }
  }

  .command-empty {
    @apply flex justify-center items-center p-1 text-xs text-gray-600;
  }
}
</style>
