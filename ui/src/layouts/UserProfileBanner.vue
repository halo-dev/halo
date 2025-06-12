<script lang="ts" setup>
import { rbacAnnotations } from "@/constants/annotations";
import { SUPER_ROLE_NAME } from "@/constants/constants";
import { useUserStore } from "@/stores/user";
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
import { storeToRefs } from "pinia";
import { computed } from "vue";
import { useI18n } from "vue-i18n";

const props = defineProps<{
  platform?: "console" | "uc";
}>();

const { t } = useI18n();
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
    <div v-if="currentUser?.spec.avatar" class="user-profile__avatar">
      <VAvatar
        :src="currentUser?.spec.avatar"
        :alt="currentUser?.spec.displayName"
        size="sm"
        circle
      ></VAvatar>
    </div>
    <div class="user-profile__info">
      <div class="user-profile__name" :title="currentUser?.spec.displayName">
        {{ currentUser?.spec.displayName }}
      </div>
      <div v-if="currentRoles?.length" class="user-profile__roles">
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
                  v-for="role in currentRoles"
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
