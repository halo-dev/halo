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
        label="是否公开注册"
        type="radio"
      ></FormKit>
      <FormKit label="默认角色" type="select"></FormKit>
    </FormKit>
  </div>
  <div class="pt-5">
    <div class="flex justify-start">
      <VButton type="secondary"> 保存</VButton>
    </div>
  </div>
</template>
