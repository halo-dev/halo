<script lang="ts" setup>
import {
  IconMore,
  IconSearch,
  IconUserSettings,
  VTag,
  VAvatar,
  Dialog,
  VDropdown,
  VDropdownItem,
} from "@halo-dev/components";
import { RoutesMenu } from "@/components/menu/RoutesMenu";
import type { MenuGroupType, MenuItemType } from "@halo-dev/console-shared";
import IconLogo from "~icons/core/logo?width=5rem&height=2rem";
import {
  RouterView,
  useRoute,
  useRouter,
  type RouteRecordRaw,
} from "vue-router";
import { onMounted, onUnmounted, reactive, ref } from "vue";
import axios from "axios";
import GlobalSearchModal from "@/components/global-search/GlobalSearchModal.vue";
import LoginModal from "@/components/login/LoginModal.vue";
import { coreMenuGroups } from "@/router/routes.config";
import sortBy from "lodash.sortby";
import { useRoleStore } from "@/stores/role";
import { hasPermission } from "@/utils/permission";
import { useUserStore } from "@/stores/user";
import { rbacAnnotations } from "@/constants/annotations";
import { defineStore, storeToRefs } from "pinia";
import { useI18n } from "vue-i18n";
import {
  useOverlayScrollbars,
  type UseOverlayScrollbarsParams,
} from "overlayscrollbars-vue";

const route = useRoute();
const router = useRouter();
const { t } = useI18n();

const moreMenuVisible = ref(false);
const moreMenuRootVisible = ref(false);

const userStore = useUserStore();

const { currentRoles, currentUser } = storeToRefs(userStore);

const handleLogout = () => {
  Dialog.warning({
    title: t("core.sidebar.operations.logout.title"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      try {
        await axios.post(`${import.meta.env.VITE_API_URL}/logout`, undefined, {
          withCredentials: true,
        });

        await userStore.fetchCurrentUser();

        router.replace({ name: "Login" });
      } catch (error) {
        console.error("Failed to logout", error);
      }
    },
  });
};

// Global Search
const globalSearchVisible = ref(false);

const isMac = /macintosh|mac os x/i.test(navigator.userAgent);

const handleGlobalSearchKeybinding = (e: KeyboardEvent) => {
  const { key, ctrlKey, metaKey } = e;
  if (key === "k" && ((ctrlKey && !isMac) || metaKey)) {
    globalSearchVisible.value = true;
    e.preventDefault();
  }
};

onMounted(() => {
  document.addEventListener("keydown", handleGlobalSearchKeybinding);
});

onUnmounted(() => {
  document.removeEventListener("keydown", handleGlobalSearchKeybinding);
});

// Generate menus by routes
const menus = ref<MenuGroupType[]>([] as MenuGroupType[]);
const minimenus = ref<MenuItemType[]>([] as MenuItemType[]);

const roleStore = useRoleStore();
const { uiPermissions } = roleStore.permissions;

const generateMenus = () => {
  // sort by menu.priority and meta.core
  const currentRoutes = sortBy(
    router.getRoutes().filter((route) => {
      const { meta } = route;
      if (!meta?.menu) {
        return false;
      }
      if (meta.permissions) {
        return hasPermission(uiPermissions, meta.permissions as string[], true);
      }
      return true;
    }),
    [
      (route: RouteRecordRaw) => !route.meta?.core,
      (route: RouteRecordRaw) => route.meta?.menu?.priority || 0,
    ]
  );

  // group by menu.group
  menus.value = currentRoutes.reduce((acc, route) => {
    const { menu } = route.meta;
    if (!menu) {
      return acc;
    }
    const group = acc.find((item) => item.id === menu.group);
    const childRoute = route.children[0];
    const childMetaMenu = childRoute?.meta?.menu;

    // only support one level
    const menuChildren = childMetaMenu
      ? [
          {
            name: childMetaMenu.name,
            path: childRoute.path,
            icon: childMetaMenu.icon,
          },
        ]
      : undefined;
    if (group) {
      group.items?.push({
        name: menu.name,
        path: route.path,
        icon: menu.icon,
        mobile: menu.mobile,
        children: menuChildren,
      });
    } else {
      const menuGroup = coreMenuGroups.find((item) => item.id === menu.group);
      let name = "";
      if (!menuGroup) {
        name = menu.group;
      } else if (menuGroup.name) {
        name = menuGroup.name;
      }
      acc.push({
        id: menuGroup?.id || menu.group,
        name: name,
        priority: menuGroup?.priority || 0,
        items: [
          {
            name: menu.name,
            path: route.path,
            icon: menu.icon,
            mobile: menu.mobile,
            children: menuChildren,
          },
        ],
      });
    }
    return acc;
  }, [] as MenuGroupType[]);

  // sort by menu.priority
  menus.value = sortBy(menus.value, [
    (menu: MenuGroupType) => {
      return coreMenuGroups.findIndex((item) => item.id === menu.id) < 0;
    },
    (menu: MenuGroupType) => menu.priority || 0,
  ]);

  minimenus.value = menus.value
    .reduce((acc, group) => {
      if (group?.items) {
        acc.push(...group.items);
      }
      return acc;
    }, [] as MenuItemType[])
    .filter((item) => item.mobile);
};

onMounted(generateMenus);

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
  <div class="flex h-full">
    <aside
      class="navbar fixed hidden h-full overflow-y-auto md:flex md:flex-col"
    >
      <div class="logo flex justify-center pb-7 pt-5">
        <a
          href="/"
          target="_blank"
          :title="$t('core.sidebar.operations.visit_homepage.title')"
        >
          <IconLogo
            class="cursor-pointer select-none transition-all hover:brightness-125"
          />
        </a>
      </div>
      <div ref="navbarScroller" class="flex-1 overflow-y-hidden">
        <div class="px-3">
          <div
            class="flex cursor-pointer items-center rounded bg-gray-100 p-2 text-gray-400 transition-all hover:text-gray-900"
            @click="globalSearchVisible = true"
          >
            <span class="mr-3">
              <IconSearch />
            </span>
            <span class="flex-1 select-none text-base font-normal">
              {{ $t("core.sidebar.search.placeholder") }}
            </span>
            <div class="text-sm">
              {{ `${isMac ? "⌘" : "Ctrl"}+K` }}
            </div>
          </div>
        </div>
        <RoutesMenu :menus="menus" />
      </div>
      <div class="profile-placeholder">
        <div class="current-profile">
          <div v-if="currentUser?.spec.avatar" class="profile-avatar">
            <VAvatar
              :src="currentUser?.spec.avatar"
              :alt="currentUser?.spec.displayName"
              size="md"
              circle
            ></VAvatar>
          </div>
          <div class="profile-name">
            <div class="flex text-sm font-medium">
              {{ currentUser?.spec.displayName }}
            </div>
            <div v-if="currentRoles?.[0]" class="flex">
              <VTag>
                <template #leftIcon>
                  <IconUserSettings />
                </template>
                {{
                  currentRoles[0].metadata.annotations?.[
                    rbacAnnotations.DISPLAY_NAME
                  ] || currentRoles[0].metadata.name
                }}
              </VTag>
            </div>
          </div>
          <VDropdown
            class="profile-control cursor-pointer rounded p-1 transition-all hover:bg-gray-100"
          >
            <IconMore />
            <template #popper>
              <VDropdownItem
                @click="
                  $router.push({
                    name: 'UserDetail',
                    params: { name: '-' },
                  })
                "
              >
                {{ $t("core.sidebar.operations.profile.button") }}
              </VDropdownItem>
              <VDropdownItem @click="handleLogout">
                {{ $t("core.sidebar.operations.logout.button") }}
              </VDropdownItem>
            </template>
          </VDropdown>
        </div>
      </div>
    </aside>

    <main class="content w-full pb-12 mb-safe md:pb-0">
      <slot v-if="$slots.default" />
      <RouterView v-else />
    </main>

    <!--bottom nav bar-->
    <div
      v-if="minimenus"
      class="bottom-nav-bar fixed bottom-0 left-0 right-0 grid grid-cols-6 border-t-2 border-black bg-secondary drop-shadow-2xl mt-safe pb-safe md:hidden"
    >
      <div
        v-for="(menu, index) in minimenus"
        :key="index"
        :class="{ 'bg-black': route.path === menu?.path }"
        class="nav-item"
        @click="router.push(menu?.path)"
      >
        <div
          class="flex w-full cursor-pointer items-center justify-center p-1 text-white"
        >
          <div class="flex h-10 w-10 flex-col items-center justify-center">
            <div class="text-base">
              <Component :is="menu?.icon" />
            </div>
          </div>
        </div>
      </div>
      <div class="nav-item" @click="moreMenuVisible = true">
        <div
          class="flex w-full cursor-pointer items-center justify-center p-1 text-white"
        >
          <div class="flex h-10 w-10 flex-col items-center justify-center">
            <div class="text-base">
              <IconMore />
            </div>
          </div>
        </div>
      </div>

      <Teleport to="body">
        <div
          v-show="moreMenuRootVisible"
          class="drawer-wrapper fixed left-0 top-0 z-[99999] flex h-full w-full flex-row items-end justify-center"
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
              class="drawer-layer absolute left-0 top-0 h-full w-full flex-none bg-gray-500 bg-opacity-75 transition-opacity"
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
                <RoutesMenu
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
  <GlobalSearchModal v-model:visible="globalSearchVisible" />
  <LoginModal />
</template>

<style lang="scss">
.navbar {
  @apply w-64;
  @apply bg-white;
  @apply shadow;
  z-index: 999;

  .profile-placeholder {
    height: 70px;

    .current-profile {
      height: 70px;
      @apply fixed
      bottom-0
      left-0
      flex
      w-64
      gap-3
      bg-white
      p-3;

      .profile-avatar {
        @apply flex
        items-center 
        self-center;
      }

      .profile-name {
        @apply flex-1
        self-center;
      }

      .profile-control {
        @apply self-center;
      }
    }
  }
}

.content {
  @apply ml-0
  flex
  flex-auto
  flex-col
  overflow-x-hidden
  md:ml-64;
}
</style>
