<script lang="ts" setup>
import {
  paginate,
  ucApiClient,
  type Notification,
  type NotificationV1alpha1UcApiListUserNotificationsRequest,
} from "@halo-dev/api-client";
import {
  Dialog,
  IconClose,
  IconDeleteBin,
  IconNotificationBadgeLine,
  Toast,
  VButton,
  VCard,
  VEmpty,
  VLoading,
  VPageHeader,
  VTabbar,
} from "@halo-dev/components";
import { stores } from "@halo-dev/ui-shared";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { useRouteQuery } from "@vueuse/router";
import { chunk } from "es-toolkit";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import RiCheckDoubleLine from "~icons/ri/check-double-line";
import RiCheckboxMultipleFill from "~icons/ri/checkbox-multiple-fill";
import RiCheckboxMultipleLine from "~icons/ri/checkbox-multiple-line";
import NotificationContent from "./components/NotificationContent.vue";
import NotificationListItem from "./components/NotificationListItem.vue";

const queryClient = useQueryClient();
const { t } = useI18n();
const { currentUser } = stores.currentUser();

const activeTab = useRouteQuery("tab", "unread");

const {
  data: notifications,
  isLoading,
  refetch,
  isFetching,
} = useQuery({
  queryKey: ["user-notifications", activeTab],
  queryFn: async () => {
    return await paginate<
      NotificationV1alpha1UcApiListUserNotificationsRequest,
      Notification
    >(
      (params) =>
        ucApiClient.notification.notification.listUserNotifications(params),
      {
        username: currentUser?.user.metadata.name as string,
        fieldSelector: [`spec.unread=${activeTab.value === "unread"}`],
        size: 1000,
      }
    );
  },
  cacheTime: 0,
  refetchInterval(data) {
    const hasDeletingNotifications = data?.some(
      (item) => item.metadata.deletionTimestamp !== undefined
    );

    return hasDeletingNotifications ? 1000 : false;
  },
});

const selectedNotificationName = useRouteQuery<string | undefined>("name");

const selectedNotification = computed(() => {
  return notifications.value?.find(
    (item) => item.metadata.name === selectedNotificationName.value
  );
});

// ---- Batch selection state ----
const isSelectMode = ref(false);
const selectedNames = ref<Set<string>>(new Set<string>());
const lastSelectedIndex = ref<number>(-1);

const isAllSelected = computed(
  () =>
    !!notifications.value?.length &&
    selectedNames.value.size === notifications.value.length
);

// Exit select mode and clear selections when switching tabs.
watch(activeTab, () => {
  isSelectMode.value = false;
  selectedNames.value.clear();
  lastSelectedIndex.value = -1;
});

// Auto-exit select mode when the list becomes empty (e.g. after deleting all).
watch(notifications, (newVal) => {
  if (isSelectMode.value && newVal !== undefined && newVal.length === 0) {
    isSelectMode.value = false;
    selectedNames.value.clear();
    lastSelectedIndex.value = -1;
  }
});

function toggleSelectMode() {
  isSelectMode.value = !isSelectMode.value;
  if (!isSelectMode.value) {
    selectedNames.value.clear();
    lastSelectedIndex.value = -1;
  }
}

/**
 * Unified click handler for notification list items.
 *
 * Normal mode: open the notification in the detail pane.
 * Select mode:
 *   - Shift+click  -> range-select from the last clicked item to the current one
 *   - Ctrl/Cmd+click -> toggle the current item without resetting the anchor
 *   - plain click  -> toggle the current item and update the anchor
 */
function handleItemClick(
  notification: Notification,
  index: number,
  event: MouseEvent
) {
  if (!isSelectMode.value) {
    selectedNotificationName.value = notification.metadata.name;
    return;
  }

  const name = notification.metadata.name;

  if (event.shiftKey && lastSelectedIndex.value !== -1) {
    const start = Math.min(lastSelectedIndex.value, index);
    const end = Math.max(lastSelectedIndex.value, index);
    // Shift+click always selects the range (deselection uses Ctrl+click).
    notifications.value?.slice(start, end + 1).forEach((n) => {
      selectedNames.value.add(n.metadata.name);
    });
    lastSelectedIndex.value = index;
  } else if (event.ctrlKey || event.metaKey) {
    // Toggle without changing the anchor (mirrors OS file-manager behaviour).
    if (selectedNames.value.has(name)) {
      selectedNames.value.delete(name);
    } else {
      selectedNames.value.add(name);
    }
  } else {
    if (selectedNames.value.has(name)) {
      selectedNames.value.delete(name);
    } else {
      selectedNames.value.add(name);
    }
    lastSelectedIndex.value = index;
  }
}

function handleSelectAll() {
  if (isAllSelected.value) {
    selectedNames.value.clear();
  } else {
    notifications.value?.forEach((n) =>
      selectedNames.value.add(n.metadata.name)
    );
  }
}

function handleDeleteSelected() {
  if (selectedNames.value.size === 0) return;

  Dialog.warning({
    title: t("core.uc_notification.operations.delete_selected.title"),
    description: t(
      "core.uc_notification.operations.delete_selected.description"
    ),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    confirmType: "danger",
    onConfirm: async () => {
      if (!currentUser) {
        throw new Error("Current user is not found");
      }

      const namesToDelete = Array.from(selectedNames.value);
      const notificationChunks = chunk(namesToDelete, 5);

      for (const chunkItem of notificationChunks) {
        await Promise.all(
          chunkItem.map((name) =>
            ucApiClient.notification.notification.deleteSpecifiedNotification({
              username: currentUser.user.metadata.name,
              name,
            })
          )
        );
      }

      selectedNames.value.clear();
      lastSelectedIndex.value = -1;
      await queryClient.invalidateQueries({ queryKey: ["user-notifications"] });
      Toast.success(t("core.common.toast.delete_success"));
    },
  });
}
// ---- End batch selection ----

function handleDeleteNotifications() {
  Dialog.warning({
    title: t("core.uc_notification.operations.delete_all.title"),
    description: t("core.uc_notification.operations.delete_all.description"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    confirmType: "danger",
    onConfirm: async () => {
      if (!notifications.value || notifications.value.length === 0) {
        return;
      }

      if (!currentUser) {
        throw new Error("Current user is not found");
      }

      const notificationChunks = chunk(notifications.value, 5);

      for (const chunkItem of notificationChunks) {
        await Promise.all(
          chunkItem.map((notification) =>
            ucApiClient.notification.notification.deleteSpecifiedNotification({
              username: currentUser.user.metadata.name,
              name: notification.metadata.name,
            })
          )
        );
      }

      await queryClient.invalidateQueries({ queryKey: ["user-notifications"] });

      Toast.success(t("core.common.toast.delete_success"));
    },
  });
}

function handleMarkAllAsRead() {
  Dialog.warning({
    title: t("core.uc_notification.operations.mark_all_as_read.title"),
    description: t(
      "core.uc_notification.operations.mark_all_as_read.description"
    ),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      if (!notifications.value || notifications.value.length === 0) {
        return;
      }

      if (!currentUser) {
        throw new Error("Current user is not found");
      }

      const names = notifications.value?.map(
        (notification) => notification.metadata.name
      );

      await ucApiClient.notification.notification.markNotificationsAsRead({
        username: currentUser.user.metadata.name,
        markSpecifiedRequest: {
          names,
        },
      });

      await queryClient.invalidateQueries({ queryKey: ["user-notifications"] });
    },
  });
}
</script>

<template>
  <VPageHeader :title="$t('core.uc_notification.title')">
    <template #icon>
      <IconNotificationBadgeLine />
    </template>
  </VPageHeader>
  <div class="m-0 md:m-4">
    <VCard
      style="height: calc(100vh - 5.5rem)"
      :body-class="['h-full', '!p-0']"
    >
      <div class="grid h-full grid-cols-12 divide-y sm:divide-x sm:divide-y-0">
        <div
          class="relative col-span-12 flex h-full flex-col overflow-hidden sm:col-span-6 lg:col-span-5 xl:col-span-3"
        >
          <div class="sticky top-0 z-10 flex-none">
            <VTabbar
              v-model:active-id="activeTab"
              class="!rounded-none"
              :items="[
                { id: 'unread', label: $t('core.uc_notification.tabs.unread') },
                { id: 'read', label: $t('core.uc_notification.tabs.read') },
              ]"
              type="outline"
              @change="selectedNotificationName = undefined"
            ></VTabbar>

            <div
              class="absolute right-4 top-1/2 flex -translate-y-1/2 items-center gap-2"
            >
              <!-- Normal mode actions -->
              <template v-if="!isSelectMode">
                <button
                  v-tooltip="
                    $t('core.uc_notification.operations.select_mode.tooltip')
                  "
                  class="flex size-7 cursor-pointer items-center justify-center rounded-full hover:bg-gray-200 disabled:pointer-events-none disabled:opacity-70"
                  :disabled="!notifications?.length"
                  @click="toggleSelectMode"
                >
                  <RiCheckboxMultipleLine
                    class="size-4 text-gray-600 group-hover:text-gray-900"
                  />
                </button>
                <button
                  v-if="activeTab === 'unread'"
                  v-tooltip="
                    $t(
                      'core.uc_notification.operations.mark_all_as_read.tooltip'
                    )
                  "
                  class="flex size-7 cursor-pointer items-center justify-center rounded-full hover:bg-gray-200 disabled:pointer-events-none disabled:opacity-70"
                  :disabled="!notifications?.length"
                  @click="handleMarkAllAsRead"
                >
                  <RiCheckDoubleLine
                    class="size-4 text-gray-600 group-hover:text-gray-900"
                  />
                </button>
                <button
                  v-tooltip="
                    $t('core.uc_notification.operations.delete_all.tooltip')
                  "
                  class="group flex size-7 cursor-pointer items-center justify-center rounded-full hover:bg-gray-200 disabled:pointer-events-none disabled:opacity-70"
                  :disabled="!notifications?.length"
                  @click="handleDeleteNotifications"
                >
                  <IconDeleteBin
                    class="size-4 text-gray-600 group-hover:text-red-600"
                  />
                </button>
              </template>

              <!-- Select mode actions -->
              <template v-else>
                <button
                  v-tooltip="
                    $t('core.uc_notification.operations.select_all.tooltip')
                  "
                  class="flex size-7 cursor-pointer items-center justify-center rounded-full hover:bg-gray-200 disabled:pointer-events-none disabled:opacity-70"
                  @click="handleSelectAll"
                >
                  <RiCheckboxMultipleFill
                    class="size-4 text-gray-600 group-hover:text-gray-900"
                  />
                </button>
                <button
                  v-tooltip="
                    $t(
                      'core.uc_notification.operations.delete_selected.tooltip'
                    )
                  "
                  class="flex size-7 cursor-pointer items-center justify-center rounded-full hover:bg-gray-200 disabled:pointer-events-none disabled:opacity-70"
                  :disabled="selectedNames.size === 0"
                  @click="handleDeleteSelected"
                >
                  <IconDeleteBin
                    class="size-4 text-gray-600 group-hover:text-red-600"
                  />
                </button>
                <button
                  v-tooltip="
                    $t(
                      'core.uc_notification.operations.exit_select_mode.tooltip'
                    )
                  "
                  class="flex size-7 cursor-pointer items-center justify-center rounded-full hover:bg-gray-200 disabled:pointer-events-none disabled:opacity-70"
                  @click="toggleSelectMode"
                >
                  <IconClose
                    class="size-4 text-gray-600 group-hover:text-gray-900"
                  />
                </button>
              </template>
            </div>
          </div>
          <OverlayScrollbarsComponent
            element="div"
            :options="{ scrollbars: { autoHide: 'scroll' } }"
            class="h-full w-full flex-1"
            defer
          >
            <VLoading v-if="isLoading" />
            <Transition v-else-if="!notifications?.length" appear name="fade">
              <VEmpty
                :title="`${
                  activeTab === 'unread'
                    ? $t('core.uc_notification.empty.titles.unread')
                    : $t('core.uc_notification.empty.titles.read')
                }`"
              >
                <template #actions>
                  <VButton :loading="isFetching && !isLoading" @click="refetch">
                    {{ $t("core.common.buttons.refresh") }}
                  </VButton>
                </template>
              </VEmpty>
            </Transition>
            <Transition v-else appear name="fade">
              <ul
                class="box-border h-full w-full divide-y divide-gray-100"
                role="list"
              >
                <li
                  v-for="(notification, index) in notifications"
                  :key="notification.metadata.name"
                  @click="handleItemClick(notification, index, $event)"
                >
                  <NotificationListItem
                    :notification="notification"
                    :is-selected="
                      !isSelectMode &&
                      selectedNotificationName === notification.metadata.name
                    "
                    :is-select-mode="isSelectMode"
                    :is-checked="selectedNames.has(notification.metadata.name)"
                  />
                </li>
              </ul>
            </Transition>
          </OverlayScrollbarsComponent>
        </div>
        <div
          class="col-span-12 overflow-auto sm:col-span-6 lg:col-span-7 xl:col-span-9"
        >
          <NotificationContent :notification="selectedNotification" />
        </div>
      </div>
    </VCard>
  </div>
</template>
