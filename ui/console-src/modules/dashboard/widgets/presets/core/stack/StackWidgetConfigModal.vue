<script setup lang="ts">
import { randomUUID } from "@/utils/id";
import ActionButton from "@console/modules/dashboard/components/ActionButton.vue";
import WidgetHubModal from "@console/modules/dashboard/components/WidgetHubModal.vue";
import {
  IconArrowDownLine,
  IconArrowUpLine,
  Toast,
  VButton,
  VModal,
  VSpace,
} from "@halo-dev/components";
import { cloneDeep } from "lodash-es";
import type { DashboardWidgetDefinition } from "packages/shared/dist";
import { onMounted, ref, toRaw, useTemplateRef } from "vue";
import WidgetEditableItem from "./components/WidgetEditableItem.vue";
import type { SimpleWidget, StackWidgetConfig } from "./types";

const props = defineProps<{
  config: StackWidgetConfig;
}>();

const emit = defineEmits<{
  (e: "close"): void;
  (e: "save", config: StackWidgetConfig): void;
}>();

const widgets = ref<SimpleWidget[]>();

onMounted(() => {
  widgets.value = toRaw(props.config.widgets);
});

const modal = useTemplateRef<InstanceType<typeof VModal> | null>("modal");

const widgetsHubModalVisible = ref(false);

function handleAddWidget(widgetDefinition: DashboardWidgetDefinition) {
  if (widgetDefinition.id === "core:stack") {
    // TODO: i18n
    Toast.error("You cannot add a stack widget to a stack widget");
    return;
  }

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

function handleSave(data: { auto_play: boolean; auto_play_interval: number }) {
  emit("save", {
    auto_play: data.auto_play,
    auto_play_interval: data.auto_play_interval,
    widgets: widgets.value || [],
  });
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
    <div class="flex flex-col gap-5">
      <FormKit
        id="stack-widget-config-form"
        type="form"
        name="stack-widget-config-form"
        :preserve="true"
        @submit="handleSave"
      >
        <FormKit
          type="checkbox"
          name="auto_play"
          label="Auto Play"
          :value="config.auto_play || false"
        />
        <FormKit
          type="number"
          number
          name="auto_play_interval"
          validation="required"
          :value="config.auto_play_interval || 3000"
          label="Interval"
        />
        <div class="py-4 flex flex-col gap-4">
          <label
            class="formkit-label block text-sm font-medium text-gray-700 formkit-invalid:text-red-500"
          >
            Widgets
          </label>
          <div class="flex flex-col gap-2 border border-dashed p-2 rounded-lg">
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

            <div class="flex justify-left">
              <VButton @click="widgetsHubModalVisible = true">
                {{ $t("core.common.buttons.add") }}
              </VButton>
            </div>
          </div>
        </div>
      </FormKit>
    </div>
    <template #footer>
      <VSpace>
        <VButton
          type="secondary"
          @click="$formkit.submit('stack-widget-config-form')"
        >
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
