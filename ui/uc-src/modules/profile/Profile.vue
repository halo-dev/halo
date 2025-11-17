<script lang="ts" setup>
import UserAvatar from "@/components/user-avatar/UserAvatar.vue";
import { usePluginModuleStore } from "@/stores/plugin";
import {
  VButton,
  VDropdown,
  VDropdownItem,
  VLoading,
  VTabbar,
} from "@halo-dev/components";
import { stores, type UserProfileTab } from "@halo-dev/ui-shared";
import { useRouteQuery } from "@vueuse/router";
import { storeToRefs } from "pinia";
import {
  computed,
  defineAsyncComponent,
  onMounted,
  ref,
  shallowRef,
} from "vue";
import { useI18n } from "vue-i18n";
import PasswordChangeModal from "./components/PasswordChangeModal.vue";
import ProfileEditingModal from "./components/ProfileEditingModal.vue";

const { t } = useI18n();

const editingModal = ref(false);
const passwordChangeModal = ref(false);

const { currentUser } = storeToRefs(stores.currentUser());
const { fetchCurrentUser } = stores.currentUser();
fetchCurrentUser();

const tabs = shallowRef<UserProfileTab[]>([
  {
    id: "detail",
    label: t("core.uc_profile.tabs.detail"),
    component: defineAsyncComponent({
      loader: () => import("./tabs/Detail.vue"),
      loadingComponent: VLoading,
    }),
    priority: 10,
  },
  {
    id: "notification-preferences",
    label: t("core.uc_profile.tabs.notification-preferences"),
    component: defineAsyncComponent({
      loader: () => import("./tabs/NotificationPreferences.vue"),
      loadingComponent: VLoading,
    }),
    priority: 20,
  },
  {
    id: "pat",
    label: t("core.uc_profile.tabs.pat"),
    component: defineAsyncComponent({
      loader: () => import("./tabs/PersonalAccessTokens.vue"),
      loadingComponent: VLoading,
    }),
    priority: 30,
  },
  {
    id: "2fa",
    label: t("core.uc_profile.tabs.2fa"),
    component: defineAsyncComponent({
      loader: () => import("./tabs/TwoFactor.vue"),
      loadingComponent: VLoading,
    }),
    priority: 40,
  },
  {
    id: "devices",
    label: t("core.uc_profile.tabs.devices"),
    component: defineAsyncComponent({
      loader: () => import("./tabs/Devices.vue"),
      loadingComponent: VLoading,
    }),
    priority: 50,
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

      tabs.value = [...tabs.value, ...providers].sort(
        (a, b) => a.priority - b.priority
      );
    } catch (error) {
      console.error(`Error processing plugin module:`, pluginModule, error);
    }
  }
});

const tabbarItems = computed(() => {
  return tabs.value.map((tab) => ({
    id: tab.id,
    label: tab.label,
  }));
});

const activeTab = useRouteQuery<string>("tab", tabs.value[0].id, {
  mode: "push",
});
</script>
<template>
  <ProfileEditingModal v-if="editingModal" @close="editingModal = false" />

  <PasswordChangeModal
    v-if="passwordChangeModal"
    @close="passwordChangeModal = false"
  />

  <header class="bg-white">
    <div class="p-4">
      <div class="flex items-center justify-between">
        <div class="flex flex-row items-center gap-5">
          <div class="group relative h-20 w-20">
            <UserAvatar
              :name="currentUser?.user.metadata.name"
              is-current-user
            />
          </div>
          <div class="block">
            <h1 class="truncate text-lg font-bold text-gray-900">
              {{ currentUser?.user.spec.displayName }}
            </h1>
            <span class="text-sm text-gray-600">
              @{{ currentUser?.user.metadata.name }}
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
        <component :is="tab.component" v-if="activeTab === tab.id" />
      </template>
    </div>
  </section>
</template>
