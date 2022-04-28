<script lang="ts" setup>
import { VTabbar } from "./index";

function initState() {
  return {
    active: "johnniang",
    items: [
      { label: "Ryan Wang", value: "ryanwang" },
      { label: "JohnNiang", value: "johnniang" },
      { label: "guqing", value: "guqing" },
    ],
  };
}
</script>
<template>
  <Story title="Tabbar" :init-state="initState">
    <template #default="{ state }">
      <div class="p-3">
        <VTabbar :items="state.items" v-model:active="state.active" />
      </div>
      <div class="p-3">
        <VTabbar
          type="pills"
          :items="state.items"
          v-model:active="state.active"
        />
      </div>
      <div class="p-3">
        <VTabbar
          type="outline"
          :items="state.items"
          v-model:active="state.active"
        />
      </div>
    </template>
  </Story>
</template>
