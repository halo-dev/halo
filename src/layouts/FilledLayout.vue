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
}
.content {
  margin-left: 260px;
}
</style>
