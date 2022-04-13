<template>
  <main class="text-red-600">Hello Halo!</main>
  <VInput v-model="value" />
  {{ value }}
</template>
<script lang="ts" setup>
import { VInput } from "@/components/base/input";
import { ref } from "vue";
const value = ref();
</script>
