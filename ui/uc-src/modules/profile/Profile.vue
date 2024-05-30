<script lang="ts" setup>
import UserAvatar from "@/components/user-avatar/UserAvatar.vue";
import { usePluginModuleStore } from "@/stores/plugin";
import { apiClient } from "@/utils/api-client";
import type { DetailedUser } from "@halo-dev/api-client";
import {
  VButton,
  VDropdown,
  VDropdownItem,
  VTabbar,
} from "@halo-dev/components";
import type { UserProfileTab } from "@halo-dev/console-shared";
import { useQuery } from "@tanstack/vue-query";
import { useRouteQuery } from "@vueuse/router";
import {
  computed,
  markRaw,
  onMounted,
  provide,
  ref,
  toRaw,
  type Ref,
} from "vue";
import { useI18n } from "vue-i18n";
import PasswordChangeModal from "./components/PasswordChangeModal.vue";
import ProfileEditingModal from "./components/ProfileEditingModal.vue";
import DetailTab from "./tabs/Detail.vue";
import NotificationPreferences from "./tabs/NotificationPreferences.vue";
import PersonalAccessTokensTab from "./tabs/PersonalAccessTokens.vue";
import TwoFactor from "./tabs/TwoFactor.vue";

const { t } = useI18n();

const editingModal = ref(false);
const passwordChangeModal = ref(false);

const {
  data: user,
  isLoading,
  refetch,
} = useQuery({
  queryKey: ["user-detail"],
  queryFn: async () => {
    const { data } = await apiClient.user.getCurrentUserDetail();
    return data;
  },
});

provide<Ref<DetailedUser | undefined>>("user", user);

const tabs = ref<UserProfileTab[]>([
  {
    id: "detail",
    label: t("core.uc_profile.tabs.detail"),
    component: markRaw(DetailTab),
    priority: 10,
  },
  {
    id: "notification-preferences",
    label: t("core.uc_profile.tabs.notification-preferences"),
    component: markRaw(NotificationPreferences),
    priority: 20,
  },
  {
    id: "pat",
    label: t("core.uc_profile.tabs.pat"),
    component: markRaw(PersonalAccessTokensTab),
    priority: 30,
  },
  {
    id: "2fa",
    label: "两步验证",
    component: markRaw(TwoFactor),
    priority: 40,
  },
]);

// Collect uc:profile:tabs:create extension points
const { pluginModules } = usePluginModuleStore();

onMounted(async () => {
  for (const pluginModule of pluginModules) {
    try {
      const callbackFunction =
        pluginModule?.extensionPoints?.["uc:user:profile:tabs:create"];
      if (typeof callbackFunction !== "function") {
        continue;
      }

      const providers = await callbackFunction();

      tabs.value.push(...providers);
    } catch (error) {
      console.error(`Error processing plugin module:`, pluginModule, error);
    }
  }
});

const tabbarItems = computed(() => {
  return toRaw(tabs)
    .value.sort((a, b) => a.priority - b.priority)
    .map((tab) => ({
      id: tab.id,
      label: tab.label,
    }));
});

const activeTab = useRouteQuery<string>("tab", tabs.value[0].id, {
  mode: "push",
});

function onPasswordChangeModalClose() {
  passwordChangeModal.value = false;
  refetch();
}
</script>
<template>
  <ProfileEditingModal v-if="editingModal" @close="editingModal = false" />

  <PasswordChangeModal
    v-if="passwordChangeModal"
    :user="user?.user"
    @close="onPasswordChangeModalClose"
  />

  <header class="bg-white">
    <div class="p-4">
      <div class="flex items-center justify-between">
        <div class="flex flex-row items-center gap-5">
          <div class="group relative h-20 w-20">
            <UserAvatar :name="user?.user.metadata.name" is-current-user />
          </div>
          <div class="block">
            <h1 class="truncate text-lg font-bold text-gray-900">
              {{ user?.user.spec.displayName }}
            </h1>
            <span v-if="!isLoading" class="text-sm text-gray-600">
              @{{ user?.user.metadata.name }}
            </span>
          </div>
        </div>
        <div>
          <VDropdown>
            <VButton type="default">
              {{ $t("core.common.buttons.edit") }}
            </VButton>
            <template #popper>
              <VDropdownItem @click="editingModal = true">
                {{ $t("core.uc_profile.actions.update_profile.title") }}
              </VDropdownItem>
              <VDropdownItem @click="passwordChangeModal = true">
                {{ $t("core.uc_profile.actions.change_password.title") }}
              </VDropdownItem>
            </template>
          </VDropdown>
        </div>
      </div>
    </div>
  </header>
  <section class="bg-white p-4">
    <VTabbar
      v-model:active-id="activeTab"
      :items="tabbarItems"
      class="w-full"
      type="outline"
    ></VTabbar>
    <div class="mt-2">
      <template v-for="tab in tabs" :key="tab.id">
        <component
          :is="tab.component"
          v-if="activeTab === tab.id"
          :user="user"
        />
      </template>
    </div>
  </section>
</template>
