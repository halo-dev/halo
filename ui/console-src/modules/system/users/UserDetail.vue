<script lang="ts" setup>
import UserAvatar from "@/components/user-avatar/UserAvatar.vue";
import { usePluginModuleStore } from "@/stores/plugin";
import { useUserStore } from "@/stores/user";
import { apiClient } from "@/utils/api-client";
import { usePermission } from "@/utils/permission";
import {
  VButton,
  VDropdown,
  VDropdownItem,
  VTabbar,
} from "@halo-dev/components";
import type { UserTab } from "@halo-dev/console-shared";
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
import { useRoute } from "vue-router";
import UserEditingModal from "./components/UserEditingModal.vue";
import UserPasswordChangeModal from "./components/UserPasswordChangeModal.vue";
import DetailTab from "./tabs/Detail.vue";

const { currentUserHasPermission } = usePermission();
const { t } = useI18n();
const { currentUser } = useUserStore();

const editingModal = ref(false);
const passwordChangeModal = ref(false);

const { params } = useRoute();

const {
  data: user,
  isLoading,
  refetch,
} = useQuery({
  queryKey: ["user-detail", params.name],
  queryFn: async () => {
    const { data } = await apiClient.user.getUserDetail({
      name: params.name as string,
    });
    return data;
  },
  enabled: computed(() => !!params.name),
});

const tabs = ref<UserTab[]>([
  {
    id: "detail",
    label: t("core.user.detail.tabs.detail"),
    component: markRaw(DetailTab),
    priority: 10,
  },
]);

// Collect user:detail:tabs:create extension points
const { pluginModules } = usePluginModuleStore();

onMounted(async () => {
  for (const pluginModule of pluginModules) {
    try {
      const callbackFunction =
        pluginModule?.extensionPoints?.["user:detail:tabs:create"];
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

const activeTab = useRouteQuery<string>("tab", tabs.value[0].id, {
  mode: "push",
});

provide<Ref<string>>("activeTab", activeTab);

const tabbarItems = computed(() => {
  return toRaw(tabs)
    .value.sort((a, b) => a.priority - b.priority)
    .map((tab) => ({
      id: tab.id,
      label: tab.label,
    }));
});

function handleRouteToUC() {
  window.location.href = "/uc";
}

function onPasswordChangeModalClose() {
  passwordChangeModal.value = false;
  refetch();
}
</script>
<template>
  <UserEditingModal
    v-if="editingModal && user?.user"
    :user="user?.user"
    @close="editingModal = false"
  />

  <UserPasswordChangeModal
    v-if="passwordChangeModal"
    :user="user?.user"
    @close="onPasswordChangeModalClose"
  />

  <header class="bg-white">
    <div class="p-4">
      <div class="flex items-center justify-between">
        <div class="flex flex-row items-center gap-5">
          <div class="group relative h-20 w-20">
            <UserAvatar :name="user?.user.metadata.name" />
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
        <div class="inline-flex items-center gap-2">
          <VButton
            v-if="currentUser?.metadata.name === user?.user.metadata.name"
            type="primary"
            @click="handleRouteToUC"
          >
            {{ $t("core.user.detail.actions.profile.title") }}
          </VButton>
          <VDropdown v-if="currentUserHasPermission(['system:users:manage'])">
            <VButton type="default">
              {{ $t("core.common.buttons.edit") }}
            </VButton>
            <template #popper>
              <VDropdownItem @click="editingModal = true">
                {{ $t("core.user.detail.actions.update_profile.title") }}
              </VDropdownItem>
              <VDropdownItem @click="passwordChangeModal = true">
                {{ $t("core.user.detail.actions.change_password.title") }}
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
