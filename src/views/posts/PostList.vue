<script lang="ts" setup>
import FilledLayout from "@/layouts/FilledLayout.vue";
import { VButton } from "@/components/base/button";
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
          <div class="table-cell font-bold">key</div>
          <div class="table-cell font-bold">value</div>
          <div class="table-cell font-bold">value</div>
          <div class="table-cell font-bold">value</div>
          <div class="table-cell font-bold">value</div>
        </div>
        <div class="table-item">
          <div class="table-cell">message</div>
          <div class="table-cell">消息文字</div>
          <div class="table-cell">消息文字</div>
          <div class="table-cell">消息文字</div>
          <div class="table-cell">消息文字</div>
        </div>
        <div class="table-item hover:bg-black hover:text-white">
          <div class="table-cell">message</div>
          <div class="table-cell">消息文字</div>
          <div class="table-cell">消息文字</div>
          <div class="table-cell">消息文字</div>
          <div class="table-cell">消息文字</div>
        </div>
        <div class="table-item">
          <div class="table-cell">message</div>
          <div class="table-cell">消息文字</div>
          <div class="table-cell w-72">
            消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字消息文字
          </div>
          <div class="table-cell">消息文字</div>
          <div class="table-cell">消息文字</div>
        </div>
        <div class="table-item">
          <div class="table-cell">message</div>
          <div class="table-cell">消息文字</div>
          <div class="table-cell">消息文字</div>
          <div class="table-cell">消息文字消息文字消息文字消息文字消息文字</div>
          <div class="table-cell">消息文字</div>
        </div>
        <div class="table-item">
          <div class="table-cell">message</div>
          <div class="table-cell">消息文字</div>
          <div class="table-cell">消息文字</div>
          <div class="table-cell">消息文字</div>
          <div class="table-cell">消息文字</div>
        </div>
        <div class="table-item">
          <div class="table-cell">message</div>
          <div class="table-cell">消息文字</div>
          <div class="table-cell">消息文字</div>
          <div class="table-cell">消息文字</div>
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
  }

  .table-cell {
    display: table-cell;
    padding: 10px;
  }
}
</style>
