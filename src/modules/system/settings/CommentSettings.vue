<script lang="ts" setup>
import { VButton } from "@halo-dev/components";
</script>
<template>
  <div class="w-1/3">
    <FormKit :actions="false" type="form">
      <FormKit
        :options="[
          { label: '启用', value: true },
          { label: '禁用', value: false },
        ]"
        :value="true"
        label="启用评论"
        type="radio"
      ></FormKit>
      <FormKit
        :options="[
          { label: '是', value: true },
          { label: '否', value: false },
        ]"
        :value="true"
        label="新评论审核"
        type="radio"
      ></FormKit>
      <FormKit
        :options="[
          { label: '是', value: true },
          { label: '否', value: false },
        ]"
        :value="true"
        label="仅限注册用户评论"
        type="radio"
      ></FormKit>
    </FormKit>
  </div>
  <div class="pt-5">
    <div class="flex justify-start">
      <VButton type="secondary"> 保存</VButton>
    </div>
  </div>
</template>
