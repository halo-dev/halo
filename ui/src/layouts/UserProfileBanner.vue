<script lang="ts" setup>
import {
  Dialog,
  IconAccountCircleLine,
  IconArrowDownLine,
  IconLogoutCircleRLine,
  IconSettings3Line,
  IconShieldUser,
  VAvatar,
  VDropdown,
  VTag,
} from "@halo-dev/components";
import { stores } from "@halo-dev/ui-shared";
import { storeToRefs } from "pinia";
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { rbacAnnotations } from "@/constants/annotations";
import { SUPER_ROLE_NAME } from "@/constants/constants";
import { useDarkModeStore } from "@/stores/useDarkModeStore";

const props = defineProps<{
  platform?: "console" | "uc";
}>();

const { t } = useI18n();

const { currentUser } = storeToRefs(stores.currentUser());

const darkModeStore = useDarkModeStore();

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

const disallowAccessConsole = computed(() => {
  if (
    currentUser.value?.roles.some(
      (role) => role.metadata.name === SUPER_ROLE_NAME
    )
  ) {
    return false;
  }

  const hasDisallowAccessConsoleRole = currentUser.value?.roles.some((role) => {
    return (
      role.metadata.annotations?.[rbacAnnotations.DISALLOW_ACCESS_CONSOLE] ===
      "true"
    );
  });
  return !!hasDisallowAccessConsoleRole;
});

const actions = computed(() => {
  const items = [
    {
      label: t("core.sidebar.operations.logout.tooltip"),
      icon: IconLogoutCircleRLine,
      onClick: handleLogout,
    },
  ];

  if (props.platform === "console") {
    items.unshift({
      label: t("core.sidebar.operations.profile.tooltip"),
      icon: IconAccountCircleLine,
      onClick: () => {
        window.location.href = "/uc";
      },
    });
  }

  if (props.platform === "uc" && !disallowAccessConsole.value) {
    items.unshift({
      label: t("core.uc_sidebar.operations.console.tooltip"),
      icon: IconSettings3Line,
      onClick: () => {
        window.location.href = "/console";
      },
    });
  }

  return items;
});
</script>
<template>
  <div class="user-profile">
    <div v-if="currentUser?.user.spec.avatar" class="user-profile__avatar">
      <VAvatar
        :key="currentUser?.user.spec.avatar"
        :src="currentUser?.user.spec.avatar"
        :alt="currentUser?.user.spec.displayName"
        size="sm"
        circle
      ></VAvatar>
    </div>
    <div class="user-profile__info">
      <div
        class="user-profile__name"
        :title="currentUser?.user.spec.displayName"
      >
        {{ currentUser?.user.spec.displayName }}
      </div>
      <div v-if="currentUser?.roles.length" class="user-profile__roles">
        <VTag v-if="currentUser.roles.length === 1">
          <template #leftIcon>
            <IconShieldUser />
          </template>
          {{
            currentUser.roles[0].metadata.annotations?.[
              rbacAnnotations.DISPLAY_NAME
            ] || currentUser.roles[0].metadata.name
          }}
        </VTag>
        <VDropdown v-else :triggers="['click']">
          <div class="user-profile__roles-dropdown">
            <VTag>
              <template #leftIcon>
                <IconShieldUser />
              </template>
              {{ $t("core.sidebar.profile.aggregate_role") }}
            </VTag>
            <IconArrowDownLine />
          </div>
          <template #popper>
            <div class="user-profile__roles-popper">
              <h2 class="user-profile__roles-title">
                {{ $t("core.sidebar.profile.aggregate_role") }}
              </h2>
              <div class="user-profile__roles-list">
                <VTag
                  v-for="role in currentUser.roles"
                  :key="role.metadata.name"
                  class="user-profile__role-tag"
                >
                  <template #leftIcon>
                    <IconShieldUser />
                  </template>
                  {{
                    role.metadata.annotations?.[rbacAnnotations.DISPLAY_NAME] ||
                    role.metadata.name
                  }}
                </VTag>
              </div>
            </div>
          </template>
        </VDropdown>
      </div>
    </div>

    <div class="user-profile__actions">
      <button
        v-tooltip="
          darkModeStore.isDark ? 'Switch to light mode' : 'Switch to dark mode'
        "
        class="user-profile__action-button"
        @click="darkModeStore.cyclePreference()"
      >
        <!-- Sun icon -->
        <svg
          v-if="darkModeStore.isDark"
          class="user-profile__action-icon"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <circle cx="12" cy="12" r="5" />
          <line x1="12" y1="1" x2="12" y2="3" />
          <line x1="12" y1="21" x2="12" y2="23" />
          <line x1="4.22" y1="4.22" x2="5.64" y2="5.64" />
          <line x1="18.36" y1="18.36" x2="19.78" y2="19.78" />
          <line x1="1" y1="12" x2="3" y2="12" />
          <line x1="21" y1="12" x2="23" y2="12" />
          <line x1="4.22" y1="19.78" x2="5.64" y2="18.36" />
          <line x1="18.36" y1="5.64" x2="19.78" y2="4.22" />
        </svg>
        <!-- Moon icon -->
        <svg
          v-else
          class="user-profile__action-icon"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
        </svg>
      </button>
      <button
        v-for="action in actions"
        :key="action.label"
        v-tooltip="action.label"
        class="user-profile__action-button"
        @click="action.onClick"
      >
        <component :is="action.icon" class="user-profile__action-icon" />
      </button>
    </div>
  </div>
</template>

<style lang="scss">
.user-profile {
  height: 70px;
  display: flex;
  width: 100%;
  gap: 0.75rem;
  background-color: theme("colors.white");
  padding: 0.75rem;

  &__avatar {
    display: flex;
    align-items: center;
    align-self: center;
  }

  &__info {
    flex: 1;
    align-self: center;
    overflow: hidden;
  }

  &__name {
    display: flex;
    font-size: 0.875rem;
    font-weight: 500;
  }

  &__roles {
    display: flex;
    margin-top: 0.25rem;
  }

  &__roles-dropdown {
    display: flex;
    gap: 0.25rem;
  }

  &__roles-popper {
    padding: 0.25rem;
  }

  &__roles-title {
    color: theme("colors.gray.600");
    font-size: 0.875rem;
    font-weight: 600;
    border-bottom: 1px solid theme("colors.gray.100");
    padding-bottom: 0.375rem;
  }

  &__roles-list {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
    margin-top: 0.5rem;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 0.25rem;
  }

  &__action-button {
    display: inline-block;
    cursor: pointer;
    border-radius: 9999px;
    padding: 0.375rem;
    transition: all;

    &:hover {
      background-color: theme("colors.gray.100");
    }
  }

  &__action-icon {
    height: 1.25rem;
    width: 1.25rem;
    color: theme("colors.gray.600");

    .user-profile__action-button:hover & {
      color: theme("colors.gray.900");
    }
  }
}
</style>
