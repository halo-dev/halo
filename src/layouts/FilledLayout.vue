<template>
  <div class="flex h-full">
    <aside class="navbar h-full" style="background: #fff">
      <div class="logo flex justify-center py-6 animate-pulse">
        <img :src="logo" style="width: 80px" alt="Halo Logo" />
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
  flex: 0 0 270px;
}
</style>
