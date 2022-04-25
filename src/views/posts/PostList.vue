<script lang="ts" setup>
import FilledLayout from "@/layouts/FilledLayout.vue";
import { VButton } from "@/components/base/button";
import { VTag } from "@/components/base/tag";
import { IconBookRead } from "@/core/icons";
</script>
<template>
  <FilledLayout>
    <div class="flex items-center justify-between p-4 bg-white">
      <div class="flex-1 self-center min-w-0">
        <h2 class="flex text-xl font-bold text-gray-800 truncate">
          <IconBookRead class="self-center mr-2" />
          <span>文章</span>
        </h2>
        <span class="text-gray-600 mt-8 text-sm">当前 10 篇已发布文章</span>
      </div>
      <div class="self-center">
        <VButton type="secondary">发布</VButton>
      </div>
    </div>

    <div class="p-4">
      <div class="table-container">
        <div class="table-item">
          <div class="table-cell font-bold"></div>
          <div class="table-cell font-bold">作者</div>
          <div class="table-cell font-bold">评论</div>
          <div class="table-cell font-bold">访问量</div>
          <div class="table-cell font-bold">发布时间</div>
        </div>
        <div v-for="i in 10" :key="i" class="table-item">
          <div class="table-cell">
            <div>Halo 1.5.2 发布</div>
            <VTag>#Halo</VTag>
          </div>
          <div class="table-cell">
            <img src="https://halo.run/avatar" class="rounded-full w-5" />
          </div>
          <div class="table-cell">0</div>
          <div class="table-cell">123</div>
          <div class="table-cell">消息文字</div>
        </div>
      </div>
    </div>
  </FilledLayout>
</template>

<style lang="scss">
.table-container {
  @apply table;
  @apply w-full;
  border: 1px solid #fff;
  background: #fff;

  border-radius: 4px;

  .table-item {
    @apply table-row;
    @apply cursor-pointer;
    @apply transition-all;

    &:hover {
      @apply bg-gray-100;
    }
  }

  .table-cell {
    display: table-cell;
    padding: 10px;
  }
}
</style>
