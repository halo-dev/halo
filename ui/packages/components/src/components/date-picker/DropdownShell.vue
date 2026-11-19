<script lang="ts" setup>
import { ref, watch } from "vue";
import { VDropdown, type VDropdownPlacement } from "../dropdown";

const props = withDefaults(
  defineProps<{
    /**
     * Controlled open state, bindable with `v-model:open`.
     */
    open?: boolean;
    disabled?: boolean;
    placement?: VDropdownPlacement;
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
    panelLabel: "Date picker",
  }
);

const emit = defineEmits<{
  (event: "update:open", value: boolean): void;
}>();

const dropdown = ref<{ show: () => void; hide: () => void }>();
const isOpen = ref(props.open ?? false);

watch(
  () => props.open,
  (value) => {
    if (value === undefined || value === isOpen.value) {
      return;
    }

    isOpen.value = value;
    emit("update:open", value);
  }
);

function onShownUpdate(value: boolean) {
  isOpen.value = value;
  emit("update:open", value);
}

function show() {
  dropdown.value?.show();
}

function hide() {
  dropdown.value?.hide();
}

defineExpose({
  show,
  hide,
});
</script>

<template>
  <VDropdown
    ref="dropdown"
    :shown="isOpen"
    :disabled="disabled"
    :placement="placement"
    :distance="4"
    :no-auto-focus="true"
    popper-class="[&_.v-popper__inner]:!overflow-hidden [&_.v-popper__inner]:!p-0"
    @update:shown="onShownUpdate"
  >
    <slot :open="isOpen" :show="show" :hide="hide" />
    <template #popper>
      <div
        role="dialog"
        aria-modal="false"
        :aria-label="panelLabel"
        class="max-w-[calc(100vw-2rem)] bg-white"
        :style="panelWidth ? { width: panelWidth } : undefined"
      >
        <slot name="panel" />
      </div>
    </template>
  </VDropdown>
</template>
