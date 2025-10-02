<script lang="ts" setup>
import GlobalSearchModal from "@/components/global-search/GlobalSearchModal.vue";
import MenuLoading from "@/components/menu/MenuLoading.vue";
import { RoutesMenu } from "@/components/menu/RoutesMenu";
import { useRouteMenuGenerator } from "@/composables/use-route-menu-generator";
import MobileMenu from "@/layouts/MobileMenu.vue";
import UserProfileBanner from "@/layouts/UserProfileBanner.vue";
import { isMac } from "@/utils/device";
import { coreMenuGroups } from "@console/router/constant";
import { IconSearch } from "@halo-dev/components";
import { useEventListener } from "@vueuse/core";
import {
  useOverlayScrollbars,
  type UseOverlayScrollbarsParams,
} from "overlayscrollbars-vue";
import { defineStore } from "pinia";
import { onMounted, reactive, ref } from "vue";
import { RouterView, useRoute } from "vue-router";
import IconLogo from "~icons/core/logo?width=5rem&height=2rem";

const route = useRoute();

// Global Search
const globalSearchVisible = ref(false);
useEventListener(document, "keydown", (e: KeyboardEvent) => {
  const { key, ctrlKey, metaKey } = e;
  if (key === "k" && ((ctrlKey && !isMac) || metaKey)) {
    globalSearchVisible.value = true;
    e.preventDefault();
  }
});

const { data, isLoading } = useRouteMenuGenerator(coreMenuGroups);

// aside scroll
const navbarScroller = ref();

const useNavbarScrollStore = defineStore("navbar", {
  state: () => ({
    y: 0,
  }),
});

const navbarScrollStore = useNavbarScrollStore();

const reactiveParams = reactive<UseOverlayScrollbarsParams>({
  options: {
    scrollbars: {
      autoHide: "scroll",
      autoHideDelay: 600,
    },
  },
  events: {
    scroll: (_, onScrollArgs) => {
      const target = onScrollArgs.target as HTMLElement;
      navbarScrollStore.y = target.scrollTop;
    },
    updated: (instance) => {
      const { viewport } = instance.elements();
      if (!viewport) return;
      viewport.scrollTo({ top: navbarScrollStore.y });
    },
  },
});
const [initialize] = useOverlayScrollbars(reactiveParams);
onMounted(() => {
  if (navbarScroller.value) {
    initialize({ target: navbarScroller.value });
  }
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
      <div ref="navbarScroller" class="sidebar__content">
        <div class="sidebar__search-wrapper">
          <div class="sidebar__search" @click="globalSearchVisible = true">
            <span class="sidebar__search-icon">
              <IconSearch />
            </span>
            <span class="sidebar__search-text">
              {{ $t("core.sidebar.search.placeholder") }}
            </span>
            <div class="sidebar__search-shortcut">
              {{ `${isMac ? "⌘" : "Ctrl"}+K` }}
            </div>
          </div>
        </div>
        <MenuLoading v-if="isLoading" />
        <RoutesMenu v-else :menus="data?.menus || []" />
      </div>
      <div class="sidebar__profile">
        <UserProfileBanner platform="console" />
      </div>
    </aside>

    <main class="main-content">
      <slot v-if="$slots.default" />
      <RouterView v-else />
      <footer v-if="!route.meta.hideFooter" class="main-content__footer">
        <span class="main-content__footer-text">Powered by </span>
        <RouterLink to="/overview" class="main-content__footer-link">
          Halo
        </RouterLink>
      </footer>
    </main>
    <MobileMenu
      :menus="data?.menus || []"
      :minimenus="data?.minimenus || []"
      platform="console"
    />
  </div>
  <GlobalSearchModal
    v-if="globalSearchVisible"
    @close="globalSearchVisible = false"
  />
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
  z-index: 999;
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
    overflow-y: hidden;
  }

  &__search-wrapper {
    padding-left: 0.75rem;
    padding-right: 0.75rem;
  }

  &__search {
    display: flex;
    cursor: pointer;
    align-items: center;
    border-radius: 0.25rem;
    background-color: theme("colors.gray.100");
    padding: 0.5rem;
    color: theme("colors.gray.400");
    transition: all;

    &:hover {
      color: theme("colors.gray.900");
    }
  }

  &__search-icon {
    margin-right: 0.75rem;
  }

  &__search-text {
    flex: 1;
    user-select: none;
    font-size: 1rem;
    font-weight: normal;
  }

  &__search-shortcut {
    font-size: 0.875rem;
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
