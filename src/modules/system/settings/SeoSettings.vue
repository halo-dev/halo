<script lang="ts" setup>
import { VButton } from "@halo-dev/components";
</script>
<template>
  <div class="w-1/3">
    <FormKit type="form">
      <FormKit
        :options="[
          { label: '是', value: true },
          { label: '否', value: false },
        ]"
        :value="false"
        label="屏蔽搜索引擎"
        type="radio"
      ></FormKit>
      <FormKit label="站点关键词" type="textarea"></FormKit>
      <FormKit label="站点描述" type="textarea"></FormKit>
    </FormKit>
  </div>
  <div class="pt-5">
    <div class="flex justify-start">
      <VButton type="secondary"> 保存</VButton>
    </div>
  </div>
</template>
