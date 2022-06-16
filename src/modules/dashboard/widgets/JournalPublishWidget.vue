<script lang="ts" setup>
import { VButton, VCard, VTextarea } from "@halo-dev/components";
</script>
<template>
  <VCard
    :bodyClass="['h-full', 'overflow-y-auto']"
    class="h-full"
    title="日志发表"
  >
    <VTextarea :rows="6" />
    <template #footer>
      <VButton type="secondary">发布</VButton>
    </template>
  </VCard>
</template>
