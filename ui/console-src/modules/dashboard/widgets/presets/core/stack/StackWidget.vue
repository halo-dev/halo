<script lang="ts" setup>
import WidgetCard from "@console/modules/dashboard/components/WidgetCard.vue";
import {
  IconArrowLeft,
  IconArrowRight,
  IconSettings,
  VButton,
} from "@halo-dev/components";
import { ref } from "vue";
import StackWidgetConfigModal from "./StackWidgetConfigModal.vue";
import WidgetViewItem from "./components/WidgetViewItem.vue";
import type { SimpleWidget } from "./types";

const props = defineProps<{
  config: {
    widgets: SimpleWidget[];
  };
  editMode?: boolean;
  previewMode?: boolean;
}>();

const emit = defineEmits<{
  (e: "update:config", config: Record<string, unknown>): void;
}>();

const configVisible = ref(false);

function handleSave(config: { widgets: SimpleWidget[] }) {
  emit("update:config", config);
}

const index = ref(0);

const handlePrev = () => {
  if (index.value <= 0) {
    index.value = props.config.widgets.length - 1;
  } else {
    index.value--;
  }
};

const handleNext = () => {
  if (index.value >= props.config.widgets.length - 1) {
    index.value = 0;
  } else {
    index.value++;
  }
};
</script>
<template>
  <WidgetCard v-if="!config.widgets?.length" title="Widget Stack">
    <div class="flex items-center justify-center w-full h-full">
      <VButton @click="configVisible = true"> Add Widget </VButton>
    </div>
  </WidgetCard>
  <div
    v-else
    class="bg-white w-full h-full rounded-lg relative group/stack-item"
  >
    <div
      v-if="editMode || previewMode"
      class="flex absolute z-10 bg-white inset-0 rounded-t-lg flex-none justify-between h-10 items-center px-4 border-b border-[#eaecf0]"
    >
      <div class="inline-flex items-center gap-2">
        <div class="text-base font-medium flex-1 shrink">Widget Stack</div>
        <IconSettings
          v-if="editMode"
          class="hover:text-gray-600 cursor-pointer"
          @click="configVisible = true"
        />
      </div>
    </div>

    <WidgetViewItem
      v-for="(widget, i) in config.widgets"
      v-show="index === i"
      :key="widget.i"
      :item="widget"
    />

    <div
      class="absolute bottom-2 left-0 right-0 z-10 flex items-center justify-center gap-6 group-hover/stack-item:opacity-100 opacity-0 transition-all duration-200"
    >
      <button
        v-if="config.widgets?.length"
        class="w-7 h-7 flex items-center justify-center rounded-full bg-black/10 hover:bg-black/20 transition-all duration-200 focus:outline-none group"
        @click="handlePrev"
      >
        <IconArrowLeft />
      </button>

      <div class="flex items-center gap-2">
        <div
          v-for="(widget, i) in config.widgets"
          :key="widget.i"
          class="group cursor-pointer"
          @click="index = i"
        >
          <div
            :class="[
              'w-2 h-2 rounded-full transition-all duration-200 ease-in-out transform',
              index === i
                ? 'bg-primary scale-150'
                : 'bg-gray-300 group-hover:bg-gray-400',
            ]"
          />
        </div>
      </div>

      <button
        v-if="config.widgets?.length"
        class="w-7 h-7 flex items-center justify-center rounded-full bg-black/10 hover:bg-black/20 transition-all duration-200 focus:outline-none group"
        @click="handleNext"
      >
        <IconArrowRight />
      </button>
    </div>
  </div>

  <StackWidgetConfigModal
    v-if="configVisible"
    :config="config"
    @close="configVisible = false"
    @save="handleSave"
  />
</template>
