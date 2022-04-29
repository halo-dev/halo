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
  <Story title="Tabbar" :init-state="initState">
    <template #default="{ state }">
      <div class="p-3">
        <VTabbar :items="state.items" v-model:activeId="state.activeId" />
      </div>
      <div class="p-3">
        <VTabbar
          type="pills"
          :items="state.items"
          v-model:activeId="state.activeId"
        />
      </div>
      <div class="p-3">
        <VTabbar
          type="outline"
          :items="state.items"
          v-model:activeId="state.activeId"
        />
      </div>
    </template>
  </Story>
</template>
