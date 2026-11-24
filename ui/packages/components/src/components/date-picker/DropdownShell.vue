<script lang="ts" setup>
import { computed, ref, watch } from "vue";
import { useComponentsI18n } from "../../locales";
import { VPopover, type VPopoverPlacement } from "../popover";

const props = withDefaults(
  defineProps<{
    /**
     * Controlled open state, bindable with `v-model:open`.
     */
    open?: boolean;
    disabled?: boolean;
    placement?: VPopoverPlacement;
    /**
     * Width of the panel, for example "20rem". By default the panel is sized
     * by its content.
     */
    panelWidth?: string;
    /**
     * Accessible label of the panel.
     */
    panelLabel?: string;
  }>(),
  {
    open: undefined,
    disabled: false,
    placement: "bottom-start",
    panelWidth: undefined,
    panelLabel: undefined,
  }
);

const { t } = useComponentsI18n();

const panelLabelText = computed(
  () => props.panelLabel ?? t("date_picker.panel")
);

const emit = defineEmits<{
  (event: "update:open", value: boolean): void;
}>();

const popover = ref<InstanceType<typeof VPopover>>();
const isOpen = ref(props.open ?? false);

watch(
  () => props.open,
  (value) => {
    if (value === undefined) {
      return;
    }

    isOpen.value = value;
  }
);

function onOpenUpdate(value: boolean) {
  isOpen.value = value;
  emit("update:open", value);
}

function show() {
  popover.value?.show();
}

function hide() {
  popover.value?.hide();
}

defineExpose({
  show,
  hide,
});
</script>

<template>
  <VPopover
    ref="popover"
    :open="isOpen"
    :disabled="disabled"
    :placement="placement"
    :offset="4"
    content-class="!p-0"
    @update:open="onOpenUpdate"
  >
    <slot :open="isOpen" :show="show" :hide="hide" />
    <template #content>
      <div
        role="group"
        :aria-label="panelLabelText"
        class="max-h-[var(--reka-popper-available-height)] max-w-[calc(100vw-2rem)] overflow-y-auto bg-white"
        :style="panelWidth ? { width: panelWidth } : undefined"
      >
        <slot name="panel" />
      </div>
    </template>
  </VPopover>
</template>
