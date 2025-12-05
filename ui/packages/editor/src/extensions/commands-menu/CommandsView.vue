<script lang="ts" setup>
import { i18n } from "@/locales";
import type { CommandMenuItemType } from "@/types";
import scrollIntoView from "scroll-into-view-if-needed";
import { ref, watch, type PropType } from "vue";

const props = defineProps({
  items: {
    type: Array as PropType<CommandMenuItemType[]>,
    required: true,
  },

  command: {
    type: Function as PropType<(item: CommandMenuItemType) => void>,
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
  <div
    class="relative flex max-h-72 w-60 flex-col gap-1 overflow-y-auto overflow-x-hidden rounded-lg border bg-white p-1.5 shadow-md"
  >
    <template v-if="items.length">
      <button
        v-for="(item, index) in items"
        :id="`command-item-${index}`"
        :key="index"
        type="button"
        :class="{ 'bg-gray-100': index === selectedIndex }"
        class="group flex w-full items-center gap-3 rounded p-1.5 transition-colors hover:bg-gray-100"
        @click="handleSelectItem(index)"
      >
        <div
          class="size-6 flex-none rounded bg-gray-100 p-1 group-hover:bg-white"
          :class="{ '!bg-white': index === selectedIndex }"
        >
          <component :is="item.icon" class="size-full" />
        </div>
        <div
          class="min-w-0 flex-1 shrink text-left text-sm text-gray-600 group-hover:font-medium group-hover:text-gray-900"
          :class="{ 'font-medium text-gray-900': index === selectedIndex }"
        >
          {{ i18n.global.t(item.title) }}
        </div>
      </button>
    </template>
    <div
      v-else
      class="flex items-center justify-center p-1 text-sm text-gray-600"
    >
      <span>
        {{ i18n.global.t("editor.extensions.commands_menu.no_results") }}
      </span>
    </div>
  </div>
</template>
