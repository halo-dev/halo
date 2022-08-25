<script lang="ts" setup>
import { VButton } from "@halo-dev/components";
</script>
<template>
  <div class="w-1/3">
    <FormKit type="form">
      <FormKit
        help="插入代码到所有页面的 head 标签部分"
        label="全局 head"
        type="textarea"
      ></FormKit>
      <FormKit
        help="插入代码到文章页面和自定义页面的 head 标签部分"
        label="内容页 head"
        type="textarea"
      ></FormKit>
      <FormKit
        help="插入代码到所有页面的页脚部分"
        label="页脚"
        type="textarea"
      ></FormKit>
    </FormKit>
  </div>
  <div class="pt-5">
    <div class="flex justify-start">
      <VButton type="secondary"> 保存</VButton>
    </div>
  </div>
</template>
