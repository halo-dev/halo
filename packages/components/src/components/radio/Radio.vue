<script lang="ts" setup>
import { computed } from "vue";

const props = defineProps({
  modelValue: {
    type: [String, Number, Boolean],
  },
  value: {
    type: [String, Number, Boolean],
  },
  label: {
    type: String,
  },
  name: {
    type: String,
  },
});

const emit = defineEmits(["update:modelValue", "change"]);

const id = ["radio", props.name, props.value]
  .filter((item) => !!item)
  .join("-");

const checked = computed(() => props.modelValue === props.value);

function handleChange(e: Event) {
  const { value } = e.target as HTMLInputElement;
  emit("update:modelValue", value);
  emit("change", value);
}
</script>
<template>
  <div :class="{ 'radio-wrapper-checked': checked }" class="radio-wrapper">
    <div class="radio-inner">
      <input
        :id="id"
        :checked="checked"
        :name="name"
        :value="value"
        type="radio"
        @change="handleChange"
      />
    </div>
    <label v-if="label" :for="id" class="radio-label">
      {{ label }}
    </label>
  </div>
</template>
<style lang="scss">
.radio-wrapper {
  @apply flex;
  @apply items-center;
  @apply box-border;
  @apply flex-grow-0;

  .radio-inner {
    @apply self-center;
    @apply relative;
  }

  .radio-label {
    @apply flex;
    @apply self-center;
    @apply items-start;
    @apply ml-3;
  }
}
</style>
