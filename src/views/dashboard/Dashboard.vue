<template>
  <VPageHeader title="仪表盘">
    <template #icon>
      <IconDashboard class="self-center mr-2" />
    </template>
    <template #actions>
      <VButton type="secondary"> 设置</VButton>
    </template>
  </VPageHeader>

  <div class="m-4">
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-3">
      <div>
        <dl class="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <VCard>
            <dt class="text-sm font-medium text-gray-500 truncate">文章</dt>
            <dd class="mt-1 text-3xl font-semibold text-gray-900">231</dd>
          </VCard>
          <VCard>
            <dt class="text-sm font-medium text-gray-500 truncate">用户</dt>
            <dd class="mt-1 text-3xl font-semibold text-gray-900">10</dd>
          </VCard>
          <VCard>
            <dt class="text-sm font-medium text-gray-500 truncate">评论</dt>
            <dd class="mt-1 text-3xl font-semibold text-gray-900">1423</dd>
          </VCard>
          <VCard>
            <dt class="text-sm font-medium text-gray-500 truncate">访问量</dt>
            <dd class="mt-1 text-3xl font-semibold text-gray-900">1232</dd>
          </VCard>
        </dl>
      </div>
      <div>
        <VCard title="快速操作">
          <ul></ul>
        </VCard>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { VButton } from "@/components/base/button";
import { VCard } from "@/components/base/card";
import { VPageHeader } from "@/components/base/header";
import { IconDashboard } from "@/core/icons";
</script>
