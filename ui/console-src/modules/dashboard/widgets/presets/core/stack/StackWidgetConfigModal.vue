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
import type { DashboardWidgetDefinition } from "@halo-dev/console-shared";
import { cloneDeep } from "lodash-es";
import { onMounted, ref, toRaw, useTemplateRef } from "vue";
import { useI18n } from "vue-i18n";
import WidgetEditableItem from "./components/WidgetEditableItem.vue";
import type { SimpleWidget, StackWidgetConfig } from "./types";

const { t } = useI18n();

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
    Toast.error(
      t("core.dashboard.widgets.presets.stack.config_modal.toast.nest_warning")
    );
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
    :title="$t('core.dashboard.widgets.presets.stack.config_modal.title')"
    :width="800"
    :centered="false"
    @close="emit('close')"
  >
    <div class="flex flex-col gap-5">
      <FormKit
        id="stack-widget-config-form"
        v-slot="{ value }"
        type="form"
        name="stack-widget-config-form"
        :preserve="true"
        @submit="handleSave"
      >
        <FormKit
          type="checkbox"
          name="auto_play"
          :label="
            $t(
              'core.dashboard.widgets.presets.stack.config_modal.fields.auto_play.label'
            )
          "
          :value="config.auto_play || false"
        />
        <FormKit
          v-if="value?.auto_play"
          type="number"
          number
          name="auto_play_interval"
          validation="required"
          :value="config.auto_play_interval || 3000"
          :label="
            $t(
              'core.dashboard.widgets.presets.stack.config_modal.fields.auto_play_interval.label'
            )
          "
        />
        <div class="flex flex-col gap-4 py-4">
          <label
            class="formkit-label block text-sm font-medium text-gray-700 formkit-invalid:text-red-500"
          >
            {{
              $t(
                "core.dashboard.widgets.presets.stack.config_modal.fields.widgets.label"
              )
            }}
          </label>
          <div class="flex flex-col gap-2 rounded-lg border border-dashed p-2">
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

            <div class="justify-left flex">
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
