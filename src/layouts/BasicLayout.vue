<template>
  <div class="flex h-full">
    <aside
      class="hidden md:block navbar fixed h-full overflow-y-auto"
      style="background: #fff"
    >
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
    <main class="content w-full overflow-y-auto mb-safe pb-12 md:pb-0">
      <slot v-if="$slots.default" />
      <RouterView v-else />
    </main>

    <!--bottom nav bar-->
    <div
      class="md:hidden bottom-nav-bar grid grid-cols-6 fixed left-0 bottom-0 right-0 border-t-2 border-black drop-shadow-2xl pb-safe mt-safe"
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
          class="p-1 w-full cursor-pointer flex items-center justify-center text-white"
        >
          <div
            class="w-10 h-10 flex flex-col justify-center items-center is-active is-active0"
          >
            <div class="text-base">
              <Component :is="menu.icon" />
            </div>
            <div class="text-xs mt-0.5">
              {{ menu.name }}
            </div>
          </div>
        </div>
      </div>
      <div class="nav-item" @click="moreMenuVisible = true">
        <div
          class="p-1 w-full cursor-pointer flex items-center justify-center text-white"
        >
          <div
            class="w-10 h-10 flex flex-col justify-center items-center is-active is-active0"
          >
            <div class="text-base">
              <IconMore />
            </div>
            <div class="text-xs mt-0.5">更多</div>
          </div>
        </div>
      </div>

      <Teleport to="body">
        <div
          v-show="moreMenuRootVisible"
          class="drawer-wrapper fixed top-0 left-0 w-full h-full flex flex-row items-end justify-center z-[99999]"
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
              class="drawer-layer flex-none absolute top-0 left-0 w-full h-full transition-opacity bg-gray-500 bg-opacity-75"
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
              class="drawer-content flex flex-col relative bg-white items-stretch shadow-xl w-screen h-3/4 rounded-t-md overflow-y-auto"
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
</template>

<script lang="ts" setup>
import { VRoutesMenu } from "@/components/base/menu";
import { VTag } from "@/components/base/tag";
import { menus, minimenus } from "@/router/menus.config";
import logo from "@/assets/logo.svg";
import { IconMore, IconUserSettings } from "@/core/icons";
import { RouterView, useRoute, useRouter } from "vue-router";
import { ref } from "vue";

const route = useRoute();
const router = useRouter();

const moreMenuVisible = ref(false);
const moreMenuRootVisible = ref(false);
</script>

<style lang="scss">
.navbar {
  @apply w-64;
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
