<template>
  <Story title="Menu">
    <Variant title="Playground">
      <template #default>
        <VMenu>
          <VMenuItem title="仪表盘">
            <template #icon>
              <Component :is="IconDashboard" />
            </template>
          </VMenuItem>
        </VMenu>
      </template>
    </Variant>
  </Story>
</template>
<script lang="ts" setup>
import { VMenu, VMenuItem } from "./index";
import { IconDashboard } from "@/core/icons";
</script>
