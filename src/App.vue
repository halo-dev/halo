<script lang="ts" setup>
import { RouterView } from "vue-router";
import { VMenu, VMenuItem, VMenuLabel } from "@/components/base/menu";
import { IconDashboard } from "@/core/icons";
</script>

<template>
  <div class="flex">
    <div class="navbar h-screen w-72 p-4" style="background: #fff">
      <VMenu :open-ids="['dashboard']">
        <VMenuLabel>首页</VMenuLabel>
        <VMenuItem id="dashboard" title="仪表盘">
          <template #icon>
            <Component :is="IconDashboard" />
          </template>
          <VMenuItem title="子菜单">
            <template #icon>
              <Component :is="IconDashboard" />
            </template>
          </VMenuItem>
          <VMenuItem title="子菜单">
            <template #icon>
              <Component :is="IconDashboard" />
            </template>
            <VMenuItem title="子菜单">
              <template #icon>
                <Component :is="IconDashboard" />
              </template>
            </VMenuItem>
          </VMenuItem>
          <VMenuItem title="子菜单">
            <template #icon>
              <Component :is="IconDashboard" />
            </template>
          </VMenuItem>
        </VMenuItem>
        <VMenuItem title="仪表盘">
          <template #icon>
            <Component :is="IconDashboard" />
          </template>
        </VMenuItem>
        <VMenuItem title="仪表盘">
          <template #icon>
            <Component :is="IconDashboard" />
          </template>
          <VMenuItem title="子菜单1">
            <template #icon>
              <Component :is="IconDashboard" />
            </template>
          </VMenuItem>
          <VMenuItem title="子菜单1">
            <template #icon>
              <Component :is="IconDashboard" />
            </template>
          </VMenuItem>
          <VMenuItem title="子菜单1">
            <template #icon>
              <Component :is="IconDashboard" />
            </template>
          </VMenuItem>
        </VMenuItem>
      </VMenu>
    </div>
    <div class="flex-1">
      <RouterView />
    </div>
  </div>
</template>

<style lang="scss">
body {
  background: #eff4f9;
}
</style>
