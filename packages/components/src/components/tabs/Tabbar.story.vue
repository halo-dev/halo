<script lang="ts" setup>
import { VTabbar } from "./index";

function initState() {
  return {
    activeId: "johnniang",
    items: [
      { label: "Ryan Wang", id: "ryanwang" },
      { label: "JohnNiang", id: "johnniang" },
      { label: "guqing", id: "guqing" },
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
