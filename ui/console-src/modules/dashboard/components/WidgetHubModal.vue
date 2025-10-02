<script setup lang="ts">
import { usePermission } from "@/utils/permission";
import { VButton, VModal, VTabbar } from "@halo-dev/components";
import type { DashboardWidgetDefinition } from "@halo-dev/console-shared";
import { computed, inject, ref, useTemplateRef, type ComputedRef } from "vue";

const { currentUserHasPermission } = usePermission();

const emit = defineEmits<{
  (event: "close"): void;
  (event: "add-widget", widget: DashboardWidgetDefinition): void;
}>();

const modal = useTemplateRef<InstanceType<typeof VModal> | null>("modal");

const activeId = ref("");

const availableWidgetDefinitions = inject<
  ComputedRef<DashboardWidgetDefinition[]>
>("availableWidgetDefinitions");

const groupedWidgetDefinitions = computed(() => {
  const filteredWidgets = availableWidgetDefinitions?.value?.filter(
    (widget) => activeId.value === "" || widget.group === activeId.value
  );

  const groups = filteredWidgets?.reduce(
    (acc, widget) => {
      const key = `${widget.defaultSize.w}-${widget.defaultSize.h}`;
      if (!acc[key]) {
        acc[key] = [];
      }
      acc[key].push(widget);
      return acc;
    },
    {} as Record<string, DashboardWidgetDefinition[]>
  );

  return Object.entries(groups || {})
    .map(([key, widgets]) => {
      const [w, h] = key.split("-").map(Number);
      return { w, h, widgets };
    })
    .sort((a, b) => {
      if (a.w !== b.w) {
        return a.w - b.w;
      }
      return a.h - b.h;
    })
    .map((group) => group.widgets);
});

const groupWidgetDefinitions = computed(() => {
  return availableWidgetDefinitions?.value?.reduce(
    (acc, item) => {
      acc[item.group] = acc[item.group] || [];
      acc[item.group].push(item);
      return acc;
    },
    {} as Record<string, DashboardWidgetDefinition[]>
  );
});

const groupWidgetDefinitionsKeys = computed(() => {
  return Object.keys(groupWidgetDefinitions.value || {});
});
</script>

<template>
  <VModal
    ref="modal"
    height="calc(100vh - 20px)"
    :width="1380"
    :layer-closable="true"
    :title="$t('core.dashboard_designer.widgets_modal.title')"
    mount-to-body
    @close="emit('close')"
  >
    <VTabbar
      v-model:active-id="activeId"
      :items="[
        { id: '', label: $t('core.common.text.all') },
        ...groupWidgetDefinitionsKeys.map((group) => {
          return { id: group, label: $t(group, group) };
        }),
      ]"
      type="outline"
    ></VTabbar>
    <div class="-m-2 mt-4 flex flex-col gap-5">
      <div
        v-for="(group, index) in groupedWidgetDefinitions"
        :key="index"
        class="flex flex-wrap"
      >
        <template v-for="item in group" :key="item.id">
          <div
            v-if="
              (activeId === '' || item.group === activeId) &&
              currentUserHasPermission(item.permissions)
            "
            :style="{
              width: `${item.defaultSize.w * 100}px`,
              height: `${item.defaultSize.h * 36}px`,
            }"
            class="cursor-pointer p-2"
            @click="emit('add-widget', item)"
          >
            <div class="pointer-events-none h-full w-full">
              <component
                :is="item.component"
                preview-mode
                :config="item.defaultConfig"
              />
            </div>
          </div>
        </template>
      </div>
    </div>
    <template #footer>
      <VButton @click="modal?.close()">
        {{ $t("core.common.buttons.close") }}
      </VButton>
    </template>
  </VModal>
</template>
