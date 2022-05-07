<template>
  <div class="flex h-full">
    <aside class="navbar fixed h-full overflow-y-auto" style="background: #fff">
      <div class="logo flex justify-center py-5">
        <img :src="logo" alt="Halo Logo" style="width: 78px" />
      </div>
      <VRoutesMenu :menus="menus" />
      <div class="current-profile">
        <div class="profile-avatar">
          <img class="h-11 w-11 rounded-full" src="https://ryanc.cc/avatar" />
        </div>
        <div class="profile-name">
          <div class="flex font-medium text-sm">Ryan Wang</div>
          <div class="flex">
            <VTag>
              <template #leftIcon>
                <IconUserSettings />
              </template>
              管理员
            </VTag>
          </div>
        </div>
        <div
          class="profile-control transition-all hover:bg-gray-100 rounded cursor-pointer p-1"
        >
          <IconMore />
        </div>
      </div>
    </aside>
    <main class="content w-full overflow-y-auto">
      <slot />
    </main>
  </div>
</template>

<script lang="ts" setup>
import { VRoutesMenu } from "@/components/base/menu";
import { VTag } from "@/components/base/tag";
import { menus } from "@/router/menus.config";
import logo from "@/assets/logo.svg";
import { IconMore, IconUserSettings } from "@/core/icons";
</script>

<style lang="scss">
.navbar {
  width: 270px;
  z-index: 999;
  box-shadow: 0 4px 4px #f6c6ce;
  padding-bottom: 70px;

  .current-profile {
    background: #fff;
    position: fixed;
    left: 0;
    bottom: 0;
    width: 270px;
    height: 70px;
    display: flex;
    @apply p-3;

    .profile-avatar {
      @apply self-center;
      @apply flex;
    }

    .profile-name {
      @apply self-center;
      @apply flex-1;
      @apply ml-3;
    }

    .profile-control {
      @apply self-center;
    }
  }
}

.content {
  margin-left: 270px;
  display: flex;
  flex: auto;
  flex-direction: column;
  overflow-x: hidden;
}
</style>
