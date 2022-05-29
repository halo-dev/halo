<script lang="ts" setup>
import { VTabItem, VTabs } from "./index";
</script>

<template>
  <Story :init-state="() => ({ activeId: 'ryanwang' })" title="Tabs">
    <template #default="{ state }">
      <div class="p-3">
        <VTabs v-model:activeId="state.activeId">
          <VTabItem id="johnniang" label="JohnNiang">
            This is JohnNiang's Item
          </VTabItem>
          <VTabItem id="ryanwang" label="Ryan Wang">
            This is Ryan Wang's Item
          </VTabItem>
          <VTabItem id="guqing" label="guqing">
            This is guqing's Item
          </VTabItem>
        </VTabs>
      </div>
      <div class="p-3">
        <VTabs v-model:activeId="state.activeId" type="pills">
          <VTabItem id="johnniang" label="JohnNiang">
            This is JohnNiang's Item
          </VTabItem>
          <VTabItem id="ryanwang" label="Ryan Wang">
            This is Ryan Wang's Item
          </VTabItem>
          <VTabItem id="guqing" label="guqing">
            This is guqing's Item
          </VTabItem>
        </VTabs>
      </div>
      <div class="p-3">
        <VTabs v-model:activeId="state.activeId" type="outline">
          <VTabItem id="johnniang" label="JohnNiang">
            This is JohnNiang's Item
          </VTabItem>
          <VTabItem id="ryanwang" label="Ryan Wang">
            This is Ryan Wang's Item
          </VTabItem>
          <VTabItem id="guqing" label="guqing">
            This is guqing's Item
          </VTabItem>
        </VTabs>
      </div>
    </template>
  </Story>
</template>
