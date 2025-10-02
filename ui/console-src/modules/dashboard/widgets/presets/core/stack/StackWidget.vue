<script lang="ts" setup>
import WidgetCard from "@console/modules/dashboard/components/WidgetCard.vue";
import { IconSettings, VButton } from "@halo-dev/components";
import { nextTick, onMounted, onUnmounted, ref } from "vue";
import StackWidgetConfigModal from "./StackWidgetConfigModal.vue";
import IndexIndicator from "./components/IndexIndicator.vue";
import WidgetViewItem from "./components/WidgetViewItem.vue";
import type { StackWidgetConfig } from "./types";

const props = defineProps<{
  config: StackWidgetConfig;
  editMode?: boolean;
  previewMode?: boolean;
}>();

const emit = defineEmits<{
  (e: "update:config", config: StackWidgetConfig): void;
}>();

const configVisible = ref(false);

const index = ref(0);

function handleNavigate(direction: -1 | 1) {
  const targetIndex = index.value + direction;

  if (targetIndex < 0) {
    index.value = props.config.widgets.length - 1;
  } else if (targetIndex >= props.config.widgets.length) {
    index.value = 0;
  } else {
    index.value = targetIndex;
  }
}

// auto play
const autoPlayInterval = ref<NodeJS.Timeout | null>(null);

function startAutoPlay() {
  if (!props.config.auto_play || configVisible.value) {
    return;
  }
  if (autoPlayInterval.value) {
    clearInterval(autoPlayInterval.value);
  }
  autoPlayInterval.value = setInterval(() => {
    handleNavigate(1);
  }, props.config.auto_play_interval || 3000);
}

function stopAutoPlay() {
  if (autoPlayInterval.value) {
    clearInterval(autoPlayInterval.value);
    autoPlayInterval.value = null;
  }
}

onMounted(() => {
  startAutoPlay();
});

onUnmounted(() => {
  stopAutoPlay();
});

async function handleSave(config: StackWidgetConfig) {
  emit("update:config", config);
  configVisible.value = false;

  await nextTick();

  if (config.auto_play) {
    startAutoPlay();
  } else {
    stopAutoPlay();
  }
}
</script>
<template>
  <WidgetCard
    v-if="!config.widgets?.length"
    :title="$t('core.dashboard.widgets.presets.stack.title')"
  >
    <div class="flex h-full w-full items-center justify-center">
      <VButton @click="configVisible = true">
        {{
          $t(
            "core.dashboard.widgets.presets.stack.operations.add_widget.button"
          )
        }}
      </VButton>
    </div>
  </WidgetCard>
  <div
    v-else
    class="group/stack-item relative h-full w-full overflow-hidden rounded-lg bg-white"
    @mouseenter="stopAutoPlay"
    @mouseleave="startAutoPlay"
  >
    <div
      v-if="editMode || previewMode"
      class="absolute inset-0 z-10 flex h-10 flex-none items-center justify-between rounded-t-lg border-b border-[#eaecf0] bg-white px-4"
    >
      <div class="inline-flex items-center gap-2">
        <div class="flex-1 shrink text-base font-medium">
          {{ $t("core.dashboard.widgets.presets.stack.title") }}
        </div>
        <IconSettings
          v-if="editMode"
          class="cursor-pointer hover:text-gray-600"
          @click="configVisible = true"
        />
      </div>
    </div>

    <TransitionGroup name="fade">
      <WidgetViewItem
        v-for="(widget, i) in config.widgets"
        v-show="index === i"
        :key="widget.i"
        :item="widget"
      />
    </TransitionGroup>

    <div
      class="absolute bottom-2 left-0 right-0 z-10 flex justify-center opacity-0 transition-all duration-200 group-hover/stack-item:opacity-100"
    >
      <IndexIndicator
        :index="index"
        :total="config.widgets.length"
        @prev="handleNavigate(-1)"
        @next="handleNavigate(1)"
        @update:index="index = $event"
      />
    </div>
  </div>

  <StackWidgetConfigModal
    v-if="configVisible"
    :config="config"
    @close="configVisible = false"
    @save="handleSave"
  />
</template>
