<script lang="ts" setup>
import { VButton } from "@halo-dev/components";
</script>
<template>
  <div class="w-1/3">
    <FormKit :actions="false" type="form">
      <FormKit label="站点标题" type="text"></FormKit>
      <FormKit label="站点副标题" type="text"></FormKit>
      <FormKit label="Logo" type="url"></FormKit>
      <FormKit label="Favicon" type="url"></FormKit>
    </FormKit>
  </div>
  <div class="pt-5">
    <div class="flex justify-start">
      <VButton type="secondary"> 保存</VButton>
    </div>
  </div>
</template>
