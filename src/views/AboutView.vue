<script lang="ts" setup>
import { FilledLayout } from "@/layouts";
import { useRoute } from "vue-router";

const route = useRoute();
</script>

<template>
  <FilledLayout>
    <div>
      <pre class="overflow-auto w-3/4">Route: {{ route }}</pre>
      <pre class="overflow-auto w-3/4">Route: {{ route }}</pre>
    </div>
  </FilledLayout>
</template>
