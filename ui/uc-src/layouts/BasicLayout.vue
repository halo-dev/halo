<script lang="ts" setup>
import { RoutesMenu } from "@/components/menu/RoutesMenu";
import { useRouteMenuGenerator } from "@/composables/use-route-menu-generator";
import { rbacAnnotations } from "@/constants/annotations";
import { SUPER_ROLE_NAME } from "@/constants/constants";
import { useUserStore } from "@/stores/user";
import { coreMenuGroups } from "@console/router/constant";
import {
  Dialog,
  IconArrowDownLine,
  IconLogoutCircleRLine,
  IconMore,
  IconSettings3Line,
  IconShieldUser,
  VAvatar,
  VDropdown,
  VTag,
} from "@halo-dev/components";
import {
  useOverlayScrollbars,
  type UseOverlayScrollbarsParams,
} from "overlayscrollbars-vue";
import { defineStore, storeToRefs } from "pinia";
import { computed, onMounted, reactive, ref } from "vue";
import { useI18n } from "vue-i18n";
import { RouterView, useRoute, useRouter } from "vue-router";
import IconLogo from "~icons/core/logo?width=5rem&height=2rem";

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
    description: t("core.sidebar.operations.logout.description"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      window.location.href = "/logout";
    },
  });
};

const { menus, minimenus } = useRouteMenuGenerator(coreMenuGroups);

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

const disallowAccessConsole = computed(() => {
  if (
    currentRoles?.value?.some((role) => role.metadata.name === SUPER_ROLE_NAME)
  ) {
    return false;
  }

  const hasDisallowAccessConsoleRole = currentRoles?.value?.some((role) => {
    return (
      role.metadata.annotations?.[rbacAnnotations.DISALLOW_ACCESS_CONSOLE] ===
      "true"
    );
  });
  return !!hasDisallowAccessConsoleRole;
});
</script>

<template>
  <div class="flex min-h-screen">
    <aside
      class="navbar fixed hidden h-full overflow-y-auto md:flex md:flex-col"
    >
      <div class="logo flex justify-center pb-5 pt-5">
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
        <RoutesMenu :menus="menus" />
      </div>
      <div class="profile-placeholder">
        <div class="current-profile">
          <div v-if="currentUser?.spec.avatar" class="profile-avatar">
            <VAvatar
              :src="currentUser?.spec.avatar"
              :alt="currentUser?.spec.displayName"
              size="sm"
              circle
            ></VAvatar>
          </div>
          <div class="profile-name">
            <div class="flex text-sm font-medium">
              {{ currentUser?.spec.displayName }}
            </div>
            <div v-if="currentRoles?.length" class="flex mt-1">
              <VTag v-if="currentRoles.length === 1">
                <template #leftIcon>
                  <IconShieldUser />
                </template>
                {{
                  currentRoles[0].metadata.annotations?.[
                    rbacAnnotations.DISPLAY_NAME
                  ] || currentRoles[0].metadata.name
                }}
              </VTag>
              <VDropdown v-else>
                <div class="flex gap-1">
                  <VTag>
                    <template #leftIcon>
                      <IconShieldUser />
                    </template>
                    {{ $t("core.uc_sidebar.profile.aggregate_role") }}
                  </VTag>
                  <IconArrowDownLine />
                </div>
                <template #popper>
                  <div class="p-1">
                    <h2
                      class="text-gray-600 text-sm font-semibold border-b border-gray-100 pb-1.5"
                    >
                      {{ $t("core.uc_sidebar.profile.aggregate_role") }}
                    </h2>
                    <div class="flex gap-2 flex-wrap mt-2">
                      <VTag
                        v-for="role in currentRoles"
                        :key="role.metadata.name"
                      >
                        <template #leftIcon>
                          <IconShieldUser />
                        </template>
                        {{
                          role.metadata.annotations?.[
                            rbacAnnotations.DISPLAY_NAME
                          ] || role.metadata.name
                        }}
                      </VTag>
                    </div>
                  </div>
                </template>
              </VDropdown>
            </div>
          </div>
          <div class="flex items-center gap-1">
            <a
              v-if="!disallowAccessConsole"
              v-tooltip="$t('core.uc_sidebar.operations.console.tooltip')"
              class="group inline-block cursor-pointer rounded-full p-1.5 transition-all hover:bg-gray-100"
              href="/console"
            >
              <IconSettings3Line
                class="h-5 w-5 text-gray-600 group-hover:text-gray-900"
              />
            </a>
            <div
              v-tooltip="$t('core.sidebar.operations.logout.tooltip')"
              class="group inline-block cursor-pointer rounded-full p-1.5 transition-all hover:bg-gray-100"
              @click="handleLogout"
            >
              <IconLogoutCircleRLine
                class="h-5 w-5 text-gray-600 group-hover:text-gray-900"
              />
            </div>
          </div>
        </div>
      </div>
    </aside>

    <main class="content w-full pb-12 mb-safe md:w-[calc(100%-16rem)] md:pb-0">
      <slot v-if="$slots.default" />
      <RouterView v-else />
      <footer
        v-if="!route.meta.hideFooter"
        class="mt-auto p-4 text-center text-sm"
      >
        <span class="text-gray-600">Powered by </span>
        <a
          href="https://www.halo.run"
          target="_blank"
          class="hover:text-gray-600"
        >
          Halo
        </a>
      </footer>
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
</template>

<style lang="scss">
.navbar {
  @apply w-64;
  @apply bg-white;
  @apply shadow;
  z-index: 999;

  .profile-placeholder {
    height: 70px;
    flex: none;

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
        self-center
        overflow-hidden;
      }
    }
  }
}

.content {
  @apply ml-0
  flex
  flex-auto
  flex-col
  md:ml-64;
}
</style>
