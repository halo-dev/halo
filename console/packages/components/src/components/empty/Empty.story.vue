<script lang="ts" setup>
import { VEmpty } from "./index";
import { VButton } from "@/components/button";
import { VSpace } from "@/components/space";
</script>
<template>
  <Story title="Empty">
    <template #default>
      <VEmpty
        message="没有找到与搜索条件匹配的文章，你可以清空搜索条件或者新建文章"
        title="没有找到与搜索条件匹配的文章"
      >
        <template #actions>
          <VSpace>
            <VButton>清空搜索</VButton>
            <VButton type="primary">新建文章</VButton>
          </VSpace>
        </template>
      </VEmpty>
    </template>
  </Story>
</template>
