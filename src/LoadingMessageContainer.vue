<script lang="ts" setup>
import type { PropType } from "vue";
import { onMounted, ref } from "vue";
import AutoAnimate from "@formkit/auto-animate";
import type { LoadingMessage } from "@/loading-message";

defineProps({
  messages: {
    type: Array as PropType<LoadingMessage[]>,
    default: () => [],
  },
});

const list = ref<HTMLElement>();

onMounted(() => {
  if (list.value) {
    AutoAnimate(list.value, {});
  }
});
</script>
<template>
  <div id="loader"></div>
  <div class="absolute right-0 bottom-10 w-96">
    <ul ref="list" class="space-y-2 text-gray-500">
      <li
        v-for="(message, index) in messages"
        :key="index"
        :class="{
          'text-red-600': message.type === 'error',
        }"
      >
        {{ message.message }}
      </li>
    </ul>
  </div>
</template>
<style>
body {
  height: 100%;
  background-color: #f5f5f5;
}

#loader {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  margin: auto;
  border: solid 3px #e5e5e5;
  border-top-color: #333;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
