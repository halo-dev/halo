<script setup lang="ts">
import { randomUUID } from "@/utils/id";
import ActionButton from "@console/modules/dashboard/components/ActionButton.vue";
import WidgetHubModal from "@console/modules/dashboard/components/WidgetHubModal.vue";
import {
  IconArrowDownLine,
  IconArrowUpLine,
  VButton,
  VModal,
  VSpace,
} from "@halo-dev/components";
import { cloneDeep } from "lodash-es";
import type { DashboardWidgetDefinition } from "packages/shared/dist";
import { onMounted, ref, toRaw, useTemplateRef } from "vue";
import WidgetEditableItem from "./components/WidgetEditableItem.vue";
import type { SimpleWidget } from "./types";

const props = defineProps<{
  config: {
    widgets: SimpleWidget[];
  };
}>();

const emit = defineEmits<{
  (e: "close"): void;
  (
    e: "save",
    config: {
      widgets: SimpleWidget[];
    }
  ): void;
}>();

const widgets = ref<SimpleWidget[]>();

onMounted(() => {
  widgets.value = toRaw(props.config.widgets);
});

const modal = useTemplateRef<InstanceType<typeof VModal> | null>("modal");

const widgetsHubModalVisible = ref(false);

function handleAddWidget(widgetDefinition: DashboardWidgetDefinition) {
  widgets.value = [
    ...(widgets.value || []),
    {
      i: randomUUID(),
      id: widgetDefinition.id,
      config: widgetDefinition.defaultConfig,
    },
  ];
  widgetsHubModalVisible.value = false;
}

function handleSave() {
  emit("save", {
    widgets: widgets.value || [],
  });
  modal.value?.close();
}

function handleRemoveWidget(widget: SimpleWidget) {
  widgets.value = widgets.value?.filter((w) => w.i !== widget.i);
}

function handleUpdateWidgetConfig(
  widget: SimpleWidget,
  config: Record<string, unknown>
) {
  widgets.value = widgets.value?.map((w) =>
    w.i === widget.i ? { ...w, config } : w
  );
}

function handleMoveWidget(widget: SimpleWidget, direction: -1 | 1) {
  const items = cloneDeep(widgets.value) || [];
  const currentIndex = items.findIndex((item) => item.i === widget.i);

  if (currentIndex === -1) return;

  const targetIndex = currentIndex + direction;

  if (targetIndex < 0 || targetIndex >= items.length) return;

  [items[currentIndex], items[targetIndex]] = [
    items[targetIndex],
    items[currentIndex],
  ];

  widgets.value = [...items];
}
</script>
<template>
  <VModal
    ref="modal"
    mount-to-body
    title="Configure Widget Stack"
    :width="800"
    :centered="false"
    @close="emit('close')"
  >
    <div>
      <div class="flex flex-col gap-2 pb-5">
        <WidgetEditableItem
          v-for="(widget, index) in widgets"
          :key="widget.id"
          :item="widget"
          @remove="handleRemoveWidget(widget)"
          @update:config="handleUpdateWidgetConfig(widget, $event)"
        >
          <template #actions>
            <ActionButton
              v-if="index > 0"
              class="bg-gray-200"
              @click="handleMoveWidget(widget, -1)"
            >
              <IconArrowUpLine class="text-gray-600" />
            </ActionButton>
            <ActionButton
              v-if="index < (widgets?.length || 0) - 1"
              class="bg-gray-200"
              @click="handleMoveWidget(widget, 1)"
            >
              <IconArrowDownLine class="text-gray-600" />
            </ActionButton>
          </template>
        </WidgetEditableItem>
      </div>

      <VButton @click="widgetsHubModalVisible = true">
        {{ $t("core.common.buttons.add") }}
      </VButton>
    </div>
    <template #footer>
      <VSpace>
        <VButton type="secondary" @click="handleSave">
          {{ $t("core.common.buttons.save") }}
        </VButton>
        <VButton @click="emit('close')">
          {{ $t("core.common.buttons.cancel") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>

  <WidgetHubModal
    v-if="widgetsHubModalVisible"
    @close="widgetsHubModalVisible = false"
    @add-widget="handleAddWidget"
  />
</template>
