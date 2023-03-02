<script lang="ts" setup>
import { VTabbar } from "./index";

function initState() {
  return {
    activeId: "general",
    items: [
      { label: "基本设置", id: "general" },
      { label: "文章设置", id: "post" },
      { label: "SEO 设置", id: "seo" },
      { label: "评论设置", id: "comment" },
      { label: "主题路由设置", id: "theme-route" },
      { label: "代码注入", id: "code-inject" },
    ],
  };
}
</script>
<template>
  <Story :init-state="initState" title="Tabbar">
    <template #default="{ state }">
      <div class="p-3">
        <VTabbar v-model:activeId="state.activeId" :items="state.items" />
      </div>
      <div class="p-3">
        <VTabbar
          v-model:activeId="state.activeId"
          :items="state.items"
          type="pills"
        />
      </div>
      <div class="p-3">
        <VTabbar
          v-model:activeId="state.activeId"
          :items="state.items"
          type="outline"
        />
      </div>
    </template>
  </Story>
</template>
