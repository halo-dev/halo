<template>
  <div class="flex h-full">
    <aside class="navbar fixed h-full overflow-y-auto" style="background: #fff">
      <div class="logo flex justify-center py-5">
        <img :src="logo" alt="Halo Logo" style="width: 78px" />
      </div>
      <VRoutesMenu :menus="menus" />
    </aside>
    <main class="content w-full overflow-y-auto">
      <slot />
    </main>
  </div>
</template>

<script lang="ts" setup>
import { VRoutesMenu } from "@/components/base/menu";
import { menus } from "@/router/menus.config";
import logo from "@/assets/logo.svg";
</script>

<style lang="scss">
.navbar {
  width: 260px;
  z-index: 999;
  box-shadow: 0 4px 4px #f6c6ce;
}
.content {
  margin-left: 260px;
  display: flex;
  flex: auto;
  flex-direction: column;
  overflow-x: hidden;
}
</style>
