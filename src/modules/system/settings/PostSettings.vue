<script lang="ts" setup>
import { VButton } from "@halo-dev/components";
</script>
<template>
  <div class="w-1/3">
    <FormKit type="form">
      <FormKit
        :options="[
          { label: '发布时间', value: '' },
          { label: '更新时间', value: '' },
          { label: '浏览量', value: '' },
        ]"
        label="默认排序方式"
        type="select"
      ></FormKit>
      <FormKit label="默认显示条数" type="number"></FormKit>
      <FormKit
        :options="[
          { label: '是', value: true },
          { label: '否', value: false },
        ]"
        :value="false"
        label="新文章审核"
        help="用户发布文章是否需要管理员审核"
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
