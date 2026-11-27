<script lang="ts" setup>
import { VScrollArea } from "@halo-dev/components";
import { coreMenuGroups } from "@uc/router/constant";
import { defineStore } from "pinia";
import { nextTick, onMounted, useTemplateRef, watch } from "vue";
import { RouterView, useRoute } from "vue-router";
import IconLogo from "~icons/core/logo?width=5rem&height=2rem";
import ImpersonationBanner from "@/components/impersonation/ImpersonationBanner.vue";
import MenuLoading from "@/components/menu/MenuLoading.vue";
import { RoutesMenu } from "@/components/menu/RoutesMenu";
import { useRouteMenuGenerator } from "@/composables/use-route-menu-generator";
import MobileMenu from "@/layouts/MobileMenu.vue";
import UserProfileBanner from "@/layouts/UserProfileBanner.vue";

const route = useRoute();

const { data, isLoading } = useRouteMenuGenerator(coreMenuGroups);

// aside scroll
interface ScrollAreaInstance {
  viewport?: HTMLElement;
}

const navbarScroller = useTemplateRef<ScrollAreaInstance>("navbarScroller");

const useNavbarScrollStore = defineStore("navbar", {
  state: () => ({
    y: 0,
  }),
});

const navbarScrollStore = useNavbarScrollStore();

function handleNavbarScroll(event: Event) {
  navbarScrollStore.y = (event.target as HTMLElement).scrollTop;
}

// Restore the offset after the menu is re-rendered, which matches the
// "updated" hook of the previous OverlayScrollbars setup.
async function restoreNavbarScroll() {
  const y = navbarScrollStore.y;
  await nextTick();
  const viewport = navbarScroller.value?.viewport;
  if (viewport) {
    viewport.scrollTop = y;
  }
}

onMounted(restoreNavbarScroll);
watch(() => [isLoading.value, data.value], restoreNavbarScroll, {
  flush: "post",
});
</script>

<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="sidebar__logo-container">
        <a
          href="/"
          target="_blank"
          :title="$t('core.sidebar.operations.visit_homepage.title')"
        >
          <IconLogo class="sidebar__logo" />
        </a>
      </div>
      <VScrollArea
        ref="navbarScroller"
        class="sidebar__content"
        @scroll="handleNavbarScroll"
      >
        <MenuLoading v-if="isLoading" />
        <RoutesMenu :menus="data?.menus || []" />
      </VScrollArea>
      <div class="sidebar__profile">
        <UserProfileBanner platform="uc" />
      </div>
    </aside>

    <main class="main-content">
      <ImpersonationBanner />
      <slot v-if="$slots.default" />
      <RouterView v-else />
      <footer v-if="!route.meta.hideFooter" class="main-content__footer">
        <span class="main-content__footer-text">Powered by </span>
        <a
          href="https://www.halo.run"
          target="_blank"
          class="main-content__footer-link"
        >
          Halo
        </a>
      </footer>
    </main>
    <MobileMenu
      :menus="data?.menus || []"
      :minimenus="data?.minimenus || []"
      platform="uc"
    />
  </div>
</template>

<style lang="scss">
.layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  position: fixed;
  width: theme("width.64");
  height: 100%;
  background-color: theme("colors.white");
  box-shadow: theme("boxShadow.DEFAULT");
  overflow-y: auto;
  display: none;
  flex-direction: column;

  @media (min-width: theme("screens.md")) {
    display: flex;
  }

  &__logo-container {
    display: flex;
    justify-content: center;
    padding-top: 1.25rem;
    padding-bottom: 1.25rem;
  }

  &__logo {
    cursor: pointer;
    user-select: none;
    transition: all;

    &:hover {
      filter: brightness(1.25);
    }
  }

  &__content {
    flex: 1;
    min-height: 0;
  }

  &__profile {
    flex: none;
  }
}

.main-content {
  width: 100%;
  padding-bottom: 3rem;
  margin-bottom: env(safe-area-inset-bottom);
  display: flex;
  flex: auto;
  flex-direction: column;

  @media (min-width: theme("screens.md")) {
    width: calc(100% - 16rem);
    margin-left: theme("width.64");
    padding-bottom: 0;
  }

  &__footer {
    margin-top: auto;
    padding: 1rem;
    text-align: center;
    font-size: 0.875rem;
  }

  &__footer-text {
    color: theme("colors.gray.600");
  }

  &__footer-link {
    &:hover {
      color: theme("colors.gray.600");
    }
  }
}
</style>
