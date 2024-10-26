<script lang="ts" setup>
import { onMounted, ref } from "vue";

const props = withDefaults(
  defineProps<{
    src?: string;
    classes?: string | string[];
  }>(),
  {
    src: undefined,
    classes: "",
  }
);

const isLoading = ref(false);
const error = ref(false);

const loadVideo = async () => {
  if (!props.src) {
    throw new Error("src is required");
  }

  const video = document.createElement("video");
  video.src = props.src;
  return new Promise((resolve, reject) => {
    video.onloadedmetadata = () => resolve(video);
    video.onerror = (err) => reject(err);
  });
};

onMounted(async () => {
  isLoading.value = true;
  try {
    await loadVideo();
  } catch (e) {
    error.value = true;
  } finally {
    isLoading.value = false;
  }
  isLoading.value = false;
});
</script>
<template>
  <template v-if="isLoading">
    <slot name="loading"> loading... </slot>
  </template>
  <template v-else-if="error">
    <slot name="error"> error </slot>
  </template>
  <video v-else :src="src" preload="metadata" :class="classes" />
</template>
