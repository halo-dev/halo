<script lang="ts" setup>
import {
  IconMore,
  IconSearch,
  IconUserSettings,
  VInput,
  VModal,
  VRoutesMenu,
  VTag,
} from "@halo-dev/components";
import type { MenuGroupType, MenuItemType } from "@/types/menus";
import logo from "@/assets/logo.svg";
import { RouterView, useRoute, useRouter } from "vue-router";
import { inject, ref } from "vue";

const menus = inject<MenuGroupType>("menus");
const minimenus = inject<MenuItemType>("minimenus");
const route = useRoute();
const router = useRouter();

const moreMenuVisible = ref(false);
const moreMenuRootVisible = ref(false);
const spotlight = ref(false);

const handleRouteToProfile = () => {
  router.push({ path: "/users/profile/detail" });
};
</script>

<template>
  <div class="flex h-full">
    <aside class="navbar fixed hidden h-full overflow-y-auto md:block">
      <div class="logo flex justify-center py-5">
        <img :src="logo" alt="Halo Logo" style="width: 78px" />
      </div>
      <div class="px-3">
        <div
          class="flex cursor-pointer items-center rounded bg-gray-100 p-2 text-gray-400 transition-all hover:text-gray-900"
          @click="spotlight = true"
        >
          <span class="mr-3">
            <IconSearch />
          </span>
          <span class="flex-1 select-none text-base font-normal">搜索</span>
          <div class="text-sm">⌘+K</div>
        </div>
      </div>
      <VRoutesMenu :menus="menus" />
      <div class="current-profile">
        <div class="profile-avatar">
          <img class="h-11 w-11 rounded-full" src="https://ryanc.cc/avatar" />
        </div>
        <div class="profile-name">
          <div class="flex text-sm font-medium">Ryan Wang</div>
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
          class="profile-control cursor-pointer rounded p-1 transition-all hover:bg-gray-100"
          @click="handleRouteToProfile"
        >
          <IconMore />
        </div>
      </div>
    </aside>
    <main class="content w-full overflow-y-auto pb-12 mb-safe md:pb-0">
      <slot v-if="$slots.default" />
      <RouterView v-else />
    </main>

    <!--bottom nav bar-->
    <div
      class="bottom-nav-bar fixed left-0 bottom-0 right-0 grid grid-cols-6 border-t-2 border-black drop-shadow-2xl mt-safe pb-safe md:hidden"
      style="background: #0e1731"
    >
      <div
        v-for="(menu, index) in minimenus"
        :key="index"
        :class="{ 'bg-black': route.path === menu.path }"
        class="nav-item"
        @click="router.push(menu.path)"
      >
        <div
          class="flex w-full cursor-pointer items-center justify-center p-1 text-white"
        >
          <div
            class="is-active is-active0 flex h-10 w-10 flex-col items-center justify-center"
          >
            <div class="text-base">
              <Component :is="menu.icon" />
            </div>
            <div class="mt-0.5 text-xs">
              {{ menu.name }}
            </div>
          </div>
        </div>
      </div>
      <div class="nav-item" @click="moreMenuVisible = true">
        <div
          class="flex w-full cursor-pointer items-center justify-center p-1 text-white"
        >
          <div
            class="is-active is-active0 flex h-10 w-10 flex-col items-center justify-center"
          >
            <div class="text-base">
              <IconMore />
            </div>
            <div class="mt-0.5 text-xs">更多</div>
          </div>
        </div>
      </div>

      <Teleport to="body">
        <div
          v-show="moreMenuRootVisible"
          class="drawer-wrapper fixed top-0 left-0 z-[99999] flex h-full w-full flex-row items-end justify-center"
        >
          <transition
            enter-active-class="ease-out duration-200"
            enter-from-class="opacity-0"
            enter-to-class="opacity-100"
            leave-active-class="ease-in duration-100"
            leave-from-class="opacity-100"
            leave-to-class="opacity-0"
            @before-enter="moreMenuRootVisible = true"
            @after-leave="moreMenuRootVisible = false"
          >
            <div
              v-show="moreMenuVisible"
              class="drawer-layer absolute top-0 left-0 h-full w-full flex-none bg-gray-500 bg-opacity-75 transition-opacity"
              @click="moreMenuVisible = false"
            ></div>
          </transition>
          <transition
            enter-active-class="transform transition ease-in-out duration-500 sm:duration-700"
            enter-from-class="translate-y-full"
            enter-to-class="translate-y-0"
            leave-active-class="transform transition ease-in-out duration-500 sm:duration-700"
            leave-from-class="translate-y-0"
            leave-to-class="translate-y-full"
          >
            <div
              v-show="moreMenuVisible"
              class="drawer-content relative flex h-3/4 w-screen flex-col items-stretch overflow-y-auto rounded-t-md bg-white shadow-xl"
            >
              <div class="drawer-body">
                <VRoutesMenu
                  :menus="menus"
                  class="p-0"
                  @select="moreMenuVisible = false"
                />
              </div>
            </div>
          </transition>
        </div>
      </Teleport>
    </div>
  </div>

  <VModal v-model:visible="spotlight" :width="600">
    <template #header>
      <VInput placeholder="全局搜索" size="lg"></VInput>
    </template>
  </VModal>
</template>

<style lang="scss">
.navbar {
  @apply w-64;
  @apply bg-white;
  z-index: 999;
  box-shadow: 0 4px 4px #f6c6ce;
  padding-bottom: 70px;

  .current-profile {
    background: #fff;
    position: fixed;
    left: 0;
    bottom: 0;
    height: 70px;
    display: flex;
    @apply w-64;
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
  @apply ml-0;
  @apply md:ml-64;
  display: flex;
  flex: auto;
  flex-direction: column;
  overflow-x: hidden;
}
</style>
