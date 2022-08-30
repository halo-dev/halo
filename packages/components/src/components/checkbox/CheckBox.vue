<script lang="ts" setup>
const props = withDefaults(
  defineProps<{
    checked?: boolean;
    value?: string | number | boolean | undefined;
    label?: string | undefined;
    name?: string | undefined;
  }>(),
  {
    checked: false,
    value: undefined,
    label: undefined,
    name: undefined,
  }
);

const id = ["checkbox", props.name, props.value]
  .filter((item) => !!item)
  .join("-");

const emit = defineEmits<{
  (event: "update:checked", value: boolean): void;
  (event: "change", value: Event): void;
}>();

function handleChange(e: Event) {
  const { checked } = e.target as HTMLInputElement;
  emit("update:checked", checked);
  emit("change", e);
}
</script>
<template>
  <div
    :class="{ 'checkbox-wrapper-checked': checked }"
    class="checkbox-wrapper"
  >
    <div class="checkbox-inner">
      <input
        :id="id"
        :checked="checked"
        :value="value"
        type="checkbox"
        @change="handleChange"
      />
    </div>
    <label v-if="label" :for="id" class="checkbox-label">
      {{ label }}
    </label>
  </div>
</template>
<style lang="scss">
.checkbox-wrapper {
  @apply flex
  items-center
  box-border
  flex-grow-0;
}
</style>
