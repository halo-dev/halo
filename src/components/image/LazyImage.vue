<script lang="ts" setup>
import { useImage } from "@vueuse/core";

const props = withDefaults(
  defineProps<{
    src: string;
    alt?: string;
    classes?: string | string[];
  }>(),
  {
    src: "",
    alt: "",
    classes: "",
  }
);

const { isLoading, error } = useImage({ src: props.src });
</script>
<template>
  <template v-if="isLoading">
    <slot name="loading"> loading... </slot>
  </template>
  <template v-else-if="error">
    <slot name="error"> error </slot>
  </template>
  <img v-else :src="src" :alt="alt" :class="classes" />
</template>
