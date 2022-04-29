<script lang="ts" setup>
import { VTabs, VTabItem } from "./index";
</script>

<template>
  <Story title="Tabs" :init-state="() => ({ activeId: 'ryanwang' })">
    <template #default="{ state }">
      <div class="p-3">
        <VTabs v-model:activeId="state.activeId">
          <VTabItem label="JohnNiang" id="johnniang">
            This is JohnNiang's Item
          </VTabItem>
          <VTabItem label="Ryan Wang" id="ryanwang">
            This is Ryan Wang's Item
          </VTabItem>
          <VTabItem label="guqing" id="guqing">
            This is guqing's Item
          </VTabItem>
        </VTabs>
      </div>
      <div class="p-3">
        <VTabs v-model:activeId="state.activeId" type="pills">
          <VTabItem label="JohnNiang" id="johnniang">
            This is JohnNiang's Item
          </VTabItem>
          <VTabItem label="Ryan Wang" id="ryanwang">
            This is Ryan Wang's Item
          </VTabItem>
          <VTabItem label="guqing" id="guqing">
            This is guqing's Item
          </VTabItem>
        </VTabs>
      </div>
      <div class="p-3">
        <VTabs v-model:activeId="state.activeId" type="outline">
          <VTabItem label="JohnNiang" id="johnniang">
            This is JohnNiang's Item
          </VTabItem>
          <VTabItem label="Ryan Wang" id="ryanwang">
            This is Ryan Wang's Item
          </VTabItem>
          <VTabItem label="guqing" id="guqing">
            This is guqing's Item
          </VTabItem>
        </VTabs>
      </div>
    </template>
  </Story>
</template>
