<template>
  <Story :init-state="initState" title="Pagination">
    <template #default="{ state }">
      <VPagination
        v-model:page="state.page"
        v-model:size="state.size"
        :total="state.total"
      ></VPagination>
    </template>
  </Story>
</template>
<script lang="ts" setup>
import { VPagination } from "./index";

function initState() {
  return {
    page: 1,
    size: 10,
    total: 100,
  };
}
</script>
