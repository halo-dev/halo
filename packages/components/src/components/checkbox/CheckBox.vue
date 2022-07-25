<script lang="ts" setup>
const props = defineProps({
  checked: {
    type: Boolean,
    default: false,
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

const id = ["checkbox", props.name, props.value]
  .filter((item) => !!item)
  .join("-");

const emit = defineEmits(["update:checked", "change"]);

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
