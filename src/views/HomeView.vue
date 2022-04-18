<template>
  <FilledLayout>
    <VInput v-model="value" />
    {{ value }}
  </FilledLayout>
</template>
<script lang="ts" setup>
import { FilledLayout } from "@/layouts";
import { VInput } from "@/components/base/input";
import { ref } from "vue";
const value = ref();
</script>
