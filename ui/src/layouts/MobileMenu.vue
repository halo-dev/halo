<script lang="ts" setup>
import { RoutesMenu } from "@/components/menu/RoutesMenu";
import { IconMore } from "@halo-dev/components";
import type { OverlayScrollbars } from "overlayscrollbars";
import type { MenuGroupType, MenuItemType } from "packages/shared/dist";
import { inject, ref, watch } from "vue";
import { useRoute } from "vue-router";
import UserProfileBanner from "./UserProfileBanner.vue";

defineProps<{
  platform?: "console" | "uc";
  menus: MenuGroupType[];
  minimenus: MenuItemType[];
}>();

const route = useRoute();

const moreMenuVisible = ref(false);

const bodyScrollInstance =
  inject<() => OverlayScrollbars | null>("bodyScrollInstance");

watch(
  () => moreMenuVisible.value,
  (value) => {
    bodyScrollInstance?.()?.options({
      overflow: {
        x: value ? "hidden" : "scroll",
        y: value ? "hidden" : "scroll",
      },
    });
  }
);
</script>
<template>
  <div v-if="minimenus" class="mobile-nav mobile-nav--fixed">
    <div
      v-for="(menu, index) in minimenus"
      :key="index"
      :class="{ 'mobile-nav__item--active': route.path === menu?.path }"
      class="mobile-nav__item"
      @click="$router.push(menu?.path)"
    >
      <div class="mobile-nav__button">
        <div class="mobile-nav__icon-container">
          <div class="mobile-nav__icon">
            <Component :is="menu?.icon" />
          </div>
        </div>
      </div>
    </div>

    <div class="mobile-nav__item" @click="moreMenuVisible = true">
      <div class="mobile-nav__button">
        <div class="mobile-nav__icon-container">
          <div class="mobile-nav__icon">
            <IconMore />
          </div>
        </div>
      </div>
    </div>

    <Teleport to="body">
      <div v-if="moreMenuVisible" class="drawer drawer--visible">
        <transition
          enter-active-class="ease-in duration-200"
          enter-from-class="opacity-0"
          enter-to-class="opacity-100"
          appear
        >
          <div class="drawer__overlay" @click="moreMenuVisible = false"></div>
        </transition>
        <transition
          enter-active-class="transform transition ease-in duration-300"
          enter-from-class="translate-y-full"
          enter-to-class="translate-y-0"
          appear
        >
          <div v-show="moreMenuVisible" class="drawer__content">
            <div class="drawer__body">
              <RoutesMenu
                :menus="menus"
                class="drawer__menu"
                @select="moreMenuVisible = false"
              />
            </div>
            <div class="drawer__footer">
              <UserProfileBanner :platform="platform" />
            </div>
          </div>
        </transition>
      </div>
    </Teleport>
  </div>
</template>

<style lang="scss">
.mobile-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  border-top: 2px solid theme("colors.black");
  background-color: theme("colors.secondary");
  box-shadow: theme("dropShadow.2xl");
  margin-top: env(safe-area-inset-top);
  padding-bottom: env(safe-area-inset-bottom);

  @media (min-width: theme("screens.md")) {
    display: none;
  }

  &__item {
    &--active {
      background-color: theme("colors.black");
    }
  }

  &__button {
    display: flex;
    width: 100%;
    cursor: pointer;
    align-items: center;
    justify-content: center;
    padding: 0.25rem;
    color: theme("colors.white");
  }

  &__icon-container {
    display: flex;
    height: 2.5rem;
    width: 2.5rem;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }

  &__icon {
    font-size: 1rem;
  }
}

.drawer {
  position: fixed;
  left: 0;
  top: 0;
  z-index: 999;
  display: flex;
  height: 100%;
  width: 100%;
  flex-direction: row;
  align-items: flex-end;
  justify-content: center;

  &__overlay {
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    width: 100%;
    flex: none;
    background-color: theme("colors.gray.500");
    opacity: 0.75;
    transition: opacity;
  }

  &__content {
    position: relative;
    display: flex;
    height: 75%;
    width: 100vw;
    flex-direction: column;
    align-items: stretch;
    border-top-left-radius: 0.75rem;
    border-top-right-radius: 0.75rem;
    background-color: theme("colors.white");
    box-shadow: theme("boxShadow.xl");
  }

  &__body {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
  }

  &__menu {
    padding: 0;
  }

  &__footer {
    width: 100%;
    flex: none;
  }
}
</style>
