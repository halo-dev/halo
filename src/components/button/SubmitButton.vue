<script lang="ts" setup>
import { VButton } from "@halo-dev/components";
import { useMagicKeys } from "@vueuse/core";
import { computed, useAttrs, watchEffect } from "vue";

const props = withDefaults(
  defineProps<{
    text?: string;
  }>(),
  {
    text: "提交",
  }
);

const emit = defineEmits<{
  (event: "submit"): void;
}>();

const isMac = /macintosh|mac os x/i.test(navigator.userAgent);

const attrs = useAttrs();

const buttonText = computed(() => {
  return `${props.text} ${isMac ? "⌘" : "Ctrl"} + ↵`;
});

const { Command_Enter, Ctrl_Enter } = useMagicKeys();

watchEffect(() => {
  if (Command_Enter.value || Ctrl_Enter.value) {
    emit("submit");
  }
});
</script>

<template>
  <VButton v-bind="attrs" @click="emit('submit')">
    {{ buttonText }}
  </VButton>
</template>
