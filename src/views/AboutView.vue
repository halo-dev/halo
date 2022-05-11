<script lang="ts" setup>
import { BasicLayout } from "@/layouts";
import { useRoute } from "vue-router";

const route = useRoute();
</script>

<template>
  <BasicLayout>
    <div>
      <pre class="overflow-auto">Route: {{ route }}</pre>
      <pre class="overflow-auto">Route: {{ route }}</pre>
    </div>
  </BasicLayout>
</template>
