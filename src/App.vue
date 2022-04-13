<script lang="ts" setup>
import { RouterView } from "vue-router";
import { VRoutesMenu } from "@/components/base/menu";
import { menus } from "@/router/menus.config";
import logo from "@/assets/logo.svg";
</script>

<template>
  <div class="flex">
    <div class="navbar h-screen w-72 p-4" style="background: #fff">
      <div class="logo flex justify-center pt-2 pb-6">
        <img :src="logo" style="width: 80px" alt="Halo Logo" />
      </div>
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
