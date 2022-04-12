<script lang="ts" setup>
import { RouterView } from "vue-router";
import { VRoutesMenu } from "@/components/base/menu";
import { menus } from "@/router/menus.config";
</script>

<template>
  <div class="flex">
    <div class="navbar h-screen w-72 p-4" style="background: #fff">
      <VRoutesMenu :menus="menus" />
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
