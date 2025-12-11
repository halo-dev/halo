<script lang="ts" setup>
import UserAvatar from "@/components/user-avatar/UserAvatar.vue";
import { usePluginModuleStore } from "@/stores/plugin";
import type { User } from "@halo-dev/api-client";
import { consoleApiClient, coreApiClient } from "@halo-dev/api-client";
import {
  Dialog,
  Toast,
  VButton,
  VDropdown,
  VDropdownDivider,
  VDropdownItem,
  VLoading,
  VTabbar,
  VTag,
} from "@halo-dev/components";
import { stores, utils, type UserTab } from "@halo-dev/ui-shared";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { useRouteQuery } from "@vueuse/router";
import {
  computed,
  defineAsyncComponent,
  onMounted,
  provide,
  ref,
  shallowRef,
  type Ref,
} from "vue";
import { useI18n } from "vue-i18n";
import { useRoute, useRouter } from "vue-router";
import GrantPermissionModal from "./components/GrantPermissionModal.vue";
import UserEditingModal from "./components/UserEditingModal.vue";
import UserPasswordChangeModal from "./components/UserPasswordChangeModal.vue";
import { useUserEnableDisable } from "./composables/use-user";

const queryClient = useQueryClient();
const { t } = useI18n();
const { currentUser } = stores.currentUser();

const editingModal = ref(false);
const passwordChangeModal = ref(false);
const grantPermissionModal = ref<boolean>(false);
const { params } = useRoute();
const router = useRouter();
const {
  data: user,
  isLoading,
  refetch,
} = useQuery({
  queryKey: ["user-detail", params.name],
  queryFn: async () => {
    const { data } = await consoleApiClient.user.getUserDetail({
      name: params.name as string,
    });
    return data;
  },
  enabled: computed(() => !!params.name),
});

const tabs = shallowRef<UserTab[]>([
  {
    id: "detail",
    label: t("core.user.detail.tabs.detail"),
    component: defineAsyncComponent({
      loader: () => import("./tabs/Detail.vue"),
      loadingComponent: VLoading,
    }),
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

      tabs.value = [...tabs.value, ...providers].sort(
        (a, b) => a.priority - b.priority
      );
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
  return tabs.value.map((tab) => ({
    id: tab.id,
    label: tab.label,
  }));
});

const handleDelete = async (user: User) => {
  Dialog.warning({
    title: t("core.user.operations.delete.title"),
    description: t("core.common.dialog.descriptions.cannot_be_recovered"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      try {
        await coreApiClient.user.deleteUser({
          name: user.metadata.name,
        });
        Toast.success(t("core.common.toast.delete_success"));
        router.push({ name: "Users" });
      } catch (e) {
        console.error("Failed to delete user", e);
      }
    },
  });
};

function handleRouteToUC() {
  window.location.href = "/uc";
}

function onPasswordChangeModalClose() {
  passwordChangeModal.value = false;
  refetch();
}
function onGrantPermissionModalClose() {
  grantPermissionModal.value = false;
  refetch();
}

const { handleEnableOrDisableUser } = useUserEnableDisable();
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

  <GrantPermissionModal
    v-if="grantPermissionModal"
    :user="user?.user"
    @close="onGrantPermissionModalClose"
  />

  <header class="bg-white">
    <div class="p-4">
      <div class="flex items-center justify-between">
        <div class="flex flex-row items-center gap-5">
          <div class="group relative h-20 w-20">
            <UserAvatar
              :name="user?.user.metadata.name"
              :is-current-user="
                user?.user.metadata.name === currentUser?.user.metadata.name
              "
            />
          </div>
          <div class="block">
            <div class="flex items-center gap-2">
              <h1 class="truncate text-lg font-bold text-gray-900">
                {{ user?.user.spec.displayName }}
              </h1>
              <VTag v-if="user?.user.spec.disabled">
                {{ $t("core.user.fields.disabled") }}
              </VTag>
            </div>
            <span v-if="!isLoading" class="text-sm text-gray-600">
              @{{ user?.user.metadata.name }}
            </span>
          </div>
        </div>
        <div class="inline-flex items-center gap-2">
          <VButton
            v-if="currentUser?.user.metadata.name === user?.user.metadata.name"
            type="primary"
            @click="handleRouteToUC"
          >
            {{ $t("core.user.detail.actions.profile.title") }}
          </VButton>
          <VDropdown v-if="utils.permission.has(['system:users:manage'])">
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
              <VDropdownItem
                v-if="
                  currentUser?.user.metadata.name !== user?.user.metadata.name
                "
                @click="grantPermissionModal = true"
              >
                {{ $t("core.user.detail.actions.grant_permission.title") }}
              </VDropdownItem>
              <VDropdownDivider
                v-if="
                  currentUser?.user.metadata.name !== user?.user.metadata.name
                "
              />
              <VDropdownItem
                v-if="
                  !!user &&
                  currentUser?.user.metadata.name !== user?.user.metadata.name
                "
                type="danger"
                @click="
                  handleEnableOrDisableUser({
                    name: user.user.metadata.name,
                    operation: user.user.spec.disabled ? 'enable' : 'disable',
                    onSuccess: () => {
                      queryClient.invalidateQueries({
                        queryKey: ['user-detail'],
                      });
                    },
                  })
                "
              >
                {{
                  user.user.spec.disabled
                    ? $t("core.user.operations.enable.title")
                    : $t("core.user.operations.disable.title")
                }}
              </VDropdownItem>
              <VDropdownItem
                v-if="
                  user &&
                  currentUser?.user.metadata.name !== user?.user.metadata.name
                "
                type="danger"
                @click="handleDelete(user.user)"
              >
                {{ $t("core.common.buttons.delete") }}
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
