<script lang="ts" setup>
import { ucApiClient, type Notification } from "@halo-dev/api-client";
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
import {
  useInfiniteQuery,
  useIsMutating,
  useMutation,
  useQuery,
  useQueryClient,
  type InfiniteData,
} from "@tanstack/vue-query";
import { useVirtualizer } from "@tanstack/vue-virtual";
import { useRouteQuery } from "@vueuse/router";
import { chunk } from "es-toolkit";
import type { OverlayScrollbars } from "overlayscrollbars";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";
import {
  computed,
  reactive,
  ref,
  watch,
  type ComponentPublicInstance,
} from "vue";
import { useI18n } from "vue-i18n";
import RiCheckDoubleLine from "~icons/ri/check-double-line";
import RiCheckboxMultipleFill from "~icons/ri/checkbox-multiple-fill";
import RiCheckboxMultipleLine from "~icons/ri/checkbox-multiple-line";
import NotificationContent from "./components/NotificationContent.vue";
import NotificationListItem from "./components/NotificationListItem.vue";
import {
  getNextNotificationPageOffset,
  NOTIFICATION_PAGE_SIZE,
} from "./utils/pagination";

const queryClient = useQueryClient();
const { t } = useI18n();
const { currentUser } = stores.currentUser();

const activeTab = useRouteQuery("tab", "unread");

type NotificationPage = {
  items: Notification[];
  tab: string;
  total: number;
  nextOffset: number;
  removalsAtFetch: number;
};

type RemovalState = {
  count: number;
  countedNames: Set<string>;
};

// Each tab keeps its own offset correction. Page keys stay aligned to
// NOTIFICATION_PAGE_SIZE; the count only matters as a difference against the
// value captured when a page was fetched.
const removalStates = reactive<Record<string, RemovalState>>({
  unread: { count: 0, countedNames: new Set<string>() },
  read: { count: 0, countedNames: new Set<string>() },
});

function removalState(tab: string): RemovalState {
  let state = removalStates[tab];

  if (!state) {
    state = { count: 0, countedNames: new Set<string>() };
    removalStates[tab] = state;
  }

  return state;
}

function registerRemoval(tab: string, name: string) {
  const state = removalState(tab);

  if (state.countedNames.has(name)) {
    return;
  }

  state.countedNames.add(name);
  state.count++;
}

function resetRemovalState(tab: string) {
  const state = removalState(tab);
  state.countedNames.clear();
  state.count = 0;
}

const {
  data: notificationPages,
  isLoading,
  refetch,
  isFetching,
  fetchNextPage,
  hasNextPage,
  isFetchingNextPage,
} = useInfiniteQuery({
  queryKey: ["user-notifications", activeTab],
  queryFn: async ({ pageParam = 0, queryKey }) => {
    const start = pageParam as number;
    const tab = queryKey[1] as string;
    // Sample before the request: a removal registered while the request is in
    // flight may not be reflected in the response, and under-subtracting would
    // skip the notification at the page boundary. Over-subtracting only
    // overlaps pages, which are deduplicated.
    const removalsAtFetch = removalState(tab).count;
    const { data } =
      await ucApiClient.notification.notification.listUserNotifications({
        username: currentUser?.user.metadata.name as string,
        page: start / NOTIFICATION_PAGE_SIZE + 1,
        size: NOTIFICATION_PAGE_SIZE,
        fieldSelector: [`spec.unread=${tab === "unread"}`],
      });

    const page: NotificationPage = {
      items: data.items,
      tab,
      total: data.total,
      nextOffset: start + data.items.length,
      removalsAtFetch,
    };

    return page;
  },
  getNextPageParam: (lastPage: NotificationPage): number | undefined =>
    getNextNotificationPageOffset(lastPage, removalState(lastPage.tab).count),
  cacheTime: 0,
});

// Notifications that were marked read or deleted locally. They stay in (or
// leave) the list without refetching; the sets also keep the rendered state
// stable when an in-flight page fetch returns stale data.
const locallyReadNames = reactive(new Set<string>());
const locallyDeletedNames = reactive(new Set<string>());
const markingReadNames = reactive(new Set<string>());

const notifications = computed(() => {
  const items =
    notificationPages.value?.pages.flatMap((page) => page.items) ?? [];
  // Aligned windows can overlap after a removal and new notifications can be
  // prepended between requests, so drop duplicates by name.
  const seen = new Set<string>();
  return items
    .filter((item) => {
      if (
        seen.has(item.metadata.name) ||
        locallyDeletedNames.has(item.metadata.name)
      ) {
        return false;
      }
      seen.add(item.metadata.name);
      return true;
    })
    .map((item) =>
      locallyReadNames.has(item.metadata.name) && item.spec?.unread
        ? { ...item, spec: { ...item.spec, unread: false } }
        : item
    );
});

const selectedNotificationName = useRouteQuery<string | undefined>("name");

const selectedNotificationFromList = computed(() =>
  notifications.value.find(
    (item) => item.metadata.name === selectedNotificationName.value
  )
);

// Deep links and notifications that dropped out of the loaded pages are
// fetched by name instead of through the list endpoint.
const { data: selectedNotificationById } = useQuery({
  queryKey: ["user-notification", selectedNotificationName],
  enabled: computed(
    () =>
      !!selectedNotificationName.value && !selectedNotificationFromList.value
  ),
  queryFn: async ({ queryKey }) => {
    const name = queryKey[1];

    if (!name) {
      return null;
    }

    const { data } =
      await ucApiClient.notification.notification.getSpecifiedNotification({
        username: currentUser?.user.metadata.name as string,
        name,
      });
    return data;
  },
});

const selectedNotification = computed(
  () =>
    selectedNotificationFromList.value ??
    selectedNotificationById.value ??
    undefined
);

function updateCachedNotificationReadState(name: string) {
  queryClient.setQueriesData<InfiniteData<NotificationPage>>(
    { queryKey: ["user-notifications"] },
    (data) => {
      // The dashboard widget shares this key prefix with a plain array.
      if (!data || !Array.isArray(data.pages)) {
        return data;
      }

      return {
        ...data,
        pages: data.pages.map((page) => ({
          ...page,
          items: page.items.map((item) =>
            item.metadata.name === name && item.spec?.unread
              ? { ...item, spec: { ...item.spec, unread: false } }
              : item
          ),
        })),
      };
    }
  );
}

function removeCachedNotifications(names: Set<string>) {
  queryClient.setQueriesData<InfiniteData<NotificationPage>>(
    { queryKey: ["user-notifications"] },
    (data) => {
      if (!data || !Array.isArray(data.pages)) {
        return data;
      }

      return {
        ...data,
        pages: data.pages.map((page) => ({
          ...page,
          items: page.items.filter((item) => !names.has(item.metadata.name)),
        })),
      };
    }
  );
}

/**
 * A local removal changes the server-side offset. Wait for an in-flight page
 * fetch to settle first, otherwise that page was requested with the old offset
 * and would skip the notification at the page boundary.
 */
function whenFetchIdle() {
  if (!isFetching.value && !isFetchingNextPage.value) {
    return Promise.resolve();
  }

  return new Promise<void>((resolve) => {
    const stop = watch(
      [isFetching, isFetchingNextPage],
      ([fetching, fetchingNextPage]) => {
        if (!fetching && !fetchingNextPage) {
          stop();
          resolve();
        }
      },
      { flush: "sync" }
    );
  });
}

type MarkAsReadVariables = {
  notification: Notification;
  tab: string;
};

const markAsReadMutation = useMutation({
  mutationKey: ["notification-mark-as-read"],
  mutationFn: async ({ notification }: MarkAsReadVariables) => {
    await whenFetchIdle();
    const { data } =
      await ucApiClient.notification.notification.markNotificationAsRead({
        username: currentUser?.user.metadata.name as string,
        name: notification.metadata.name,
      });
    return data;
  },
  onSuccess(notification, { tab }) {
    markingReadNames.delete(notification.metadata.name);
    locallyReadNames.add(notification.metadata.name);

    if (tab === "unread") {
      registerRemoval("unread", notification.metadata.name);
    }

    updateCachedNotificationReadState(notification.metadata.name);
    queryClient.setQueryData(
      ["user-notification", notification.metadata.name],
      notification
    );
  },
  onError(_, { notification }) {
    markingReadNames.delete(notification.metadata.name);
  },
});

function handleMarkAsRead(notification: Notification) {
  const name = notification.metadata.name;

  if (markingReadNames.has(name) || locallyReadNames.has(name)) {
    return;
  }

  markingReadNames.add(name);
  markAsReadMutation.mutate({ notification, tab: activeTab.value });
}

// Mark the selected notification as read here instead of inside the list item
// so it also works for notifications that are loaded but not rendered by the
// virtualizer, or not loaded at all (deep link).
watch(
  selectedNotification,
  (notification) => {
    if (
      notification?.spec?.unread &&
      !locallyReadNames.has(notification.metadata.name) &&
      !markingReadNames.has(notification.metadata.name)
    ) {
      markingReadNames.add(notification.metadata.name);
      markAsReadMutation.mutate({
        notification,
        tab: activeTab.value,
      });
    }
  },
  { immediate: true }
);

type DeleteVariables = {
  notification: Notification;
  tab: string;
};

const deleteNotificationMutation = useMutation({
  mutationKey: ["notification-delete"],
  mutationFn: async ({ notification }: DeleteVariables) => {
    await whenFetchIdle();
    await ucApiClient.notification.notification.deleteSpecifiedNotification({
      username: currentUser?.user.metadata.name as string,
      name: notification.metadata.name,
    });
    return notification.metadata.name;
  },
  onSuccess(name, { tab }) {
    locallyDeletedNames.add(name);
    registerRemoval(tab, name);
    removeCachedNotifications(new Set([name]));
    queryClient.removeQueries({ queryKey: ["user-notification", name] });

    if (selectedNotificationName.value === name) {
      selectedNotificationName.value = undefined;
    }
  },
});

function handleDeleteNotification(notification: Notification) {
  // Pin the tab so a tab switch while the dialog is open does not register
  // the removal against the wrong list.
  const tab = activeTab.value;
  Dialog.warning({
    title: t("core.uc_notification.operations.delete.title"),
    description: t("core.uc_notification.operations.delete.description"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    confirmType: "danger",
    async onConfirm() {
      await deleteNotificationMutation.mutateAsync({
        notification,
        tab,
      });
      Toast.success(t("core.common.toast.delete_success"));
    },
  });
}

const deleteSelectedMutation = useMutation({
  mutationKey: ["notification-delete"],
  mutationFn: async ({ names }: { names: string[]; tab: string }) => {
    await whenFetchIdle();

    for (const nameChunk of chunk(names, 5)) {
      await Promise.all(
        nameChunk.map((name) =>
          ucApiClient.notification.notification.deleteSpecifiedNotification({
            username: currentUser?.user.metadata.name as string,
            name,
          })
        )
      );
    }

    return names;
  },
  onSuccess(names, { tab }) {
    const deletedNames = new Set(names);

    for (const name of names) {
      locallyDeletedNames.add(name);
      registerRemoval(tab, name);
    }

    removeCachedNotifications(deletedNames);

    for (const name of names) {
      queryClient.removeQueries({ queryKey: ["user-notification", name] });
    }

    if (names.includes(selectedNotificationName.value ?? "")) {
      selectedNotificationName.value = undefined;
    }
  },
});

const markAllAsReadMutation = useMutation({
  mutationKey: ["notification-bulk"],
  mutationFn: async () => {
    await ucApiClient.notification.notification.markAllNotificationsAsRead({
      username: currentUser?.user.metadata.name as string,
    });
  },
  async onSuccess() {
    resetRemovalState("unread");
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ["user-notifications"] }),
      queryClient.invalidateQueries({ queryKey: ["user-notification"] }),
    ]);
  },
});

const deleteAllMutation = useMutation({
  mutationKey: ["notification-bulk"],
  mutationFn: async (tab: string) => {
    await ucApiClient.notification.notification.deleteAllNotifications({
      username: currentUser?.user.metadata.name as string,
      fieldSelector: [`spec.unread=${tab === "unread"}`],
    });
    return tab;
  },
  async onSuccess(tab) {
    resetRemovalState(tab);
    selectedNotificationName.value = undefined;
    await queryClient.invalidateQueries({ queryKey: ["user-notifications"] });
  },
});

const markingAsReadCount = useIsMutating({
  mutationKey: ["notification-mark-as-read"],
});
const deletingCount = useIsMutating({ mutationKey: ["notification-delete"] });
const bulkMutationCount = useIsMutating({ mutationKey: ["notification-bulk"] });

const localMutationCount = computed(
  () => markingAsReadCount.value + deletingCount.value + bulkMutationCount.value
);

// ---- Virtual scrolling ----
const scrollElement = ref<HTMLElement | null>(null);

function handleScrollbarsInitialized(instance: OverlayScrollbars) {
  scrollElement.value = instance.elements().viewport;
}

const virtualizerOptions = computed(() => ({
  count: notifications.value.length,
  getScrollElement: () => scrollElement.value,
  getItemKey: (index: number) =>
    notifications.value[index]?.metadata.name ?? index,
  estimateSize: () => 88,
  overscan: 5,
}));

const virtualizer = useVirtualizer(virtualizerOptions);

const virtualRows = computed(() =>
  virtualizer.value.getVirtualItems().flatMap((virtualItem) => {
    const notification = notifications.value[virtualItem.index];
    return notification ? [{ ...virtualItem, notification }] : [];
  })
);

const totalSize = computed(() => virtualizer.value.getTotalSize());

function measureElement(el: Element | ComponentPublicInstance | null) {
  virtualizer.value.measureElement(el instanceof Element ? el : null);
}

// Level-triggered: keep loading while the last rendered row is the last loaded
// notification, so a failed or all-duplicate page does not stall pagination.
const shouldLoadMore = computed(() => {
  const lastRow = virtualRows.value[virtualRows.value.length - 1];
  return !!lastRow && lastRow.index >= notifications.value.length - 1;
});

const isLoadingMore = ref(false);
const loadMoreFailed = ref(false);

async function loadNextPage() {
  if (isLoadingMore.value || isFetching.value || localMutationCount.value > 0) {
    return;
  }

  isLoadingMore.value = true;

  try {
    const result = await fetchNextPage({ cancelRefetch: false });
    if (result.isError) {
      loadMoreFailed.value = true;
    }
  } finally {
    isLoadingMore.value = false;
  }
}

watch(
  [
    shouldLoadMore,
    () => hasNextPage?.value,
    isFetching,
    isFetchingNextPage,
    localMutationCount,
    () => notifications.value.length,
    isLoadingMore,
    loadMoreFailed,
  ],
  () => {
    if (
      !shouldLoadMore.value ||
      hasNextPage?.value !== true ||
      isFetching.value ||
      isFetchingNextPage.value ||
      localMutationCount.value > 0 ||
      isLoadingMore.value ||
      loadMoreFailed.value
    ) {
      return;
    }

    void loadNextPage();
  },
  { immediate: true }
);

// Allow retrying by scrolling away from the end and back.
watch(shouldLoadMore, (value) => {
  if (!value) {
    loadMoreFailed.value = false;
  }
});

function retryLoadMore() {
  loadMoreFailed.value = false;
}
// ---- End virtual scrolling ----

// ---- Batch selection state ----
const isSelectMode = ref(false);
const selectedNames = ref<Set<string>>(new Set<string>());
const lastSelectedIndex = ref<number>(-1);

const isAllSelected = computed(
  () =>
    !!notifications.value.length &&
    selectedNames.value.size === notifications.value.length
);

// Exit select mode and clear selections when switching tabs.
watch(activeTab, () => {
  isSelectMode.value = false;
  selectedNames.value.clear();
  lastSelectedIndex.value = -1;
  loadMoreFailed.value = false;
  scrollElement.value?.scrollTo({ top: 0 });
});

// Auto-exit select mode when the list becomes empty (e.g. after deleting all).
watch(notifications, (newVal) => {
  if (isSelectMode.value && newVal.length === 0) {
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
    notifications.value.slice(start, end + 1).forEach((n) => {
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
    notifications.value.forEach((n) =>
      selectedNames.value.add(n.metadata.name)
    );
  }
}

function handleDeleteSelected() {
  if (selectedNames.value.size === 0) {
    return;
  }

  // Pin the tab for the same reason as handleDeleteNotification.
  const tab = activeTab.value;
  Dialog.warning({
    title: t("core.uc_notification.operations.delete_selected.title"),
    description: t(
      "core.uc_notification.operations.delete_selected.description"
    ),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    confirmType: "danger",
    onConfirm: async () => {
      await deleteSelectedMutation.mutateAsync({
        names: Array.from(selectedNames.value),
        tab,
      });

      selectedNames.value.clear();
      lastSelectedIndex.value = -1;
      Toast.success(t("core.common.toast.delete_success"));
    },
  });
}

function handleDeleteNotifications() {
  Dialog.warning({
    title: t("core.uc_notification.operations.delete_all.title"),
    description: t("core.uc_notification.operations.delete_all.description"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    confirmType: "danger",
    onConfirm: async () => {
      await deleteAllMutation.mutateAsync(activeTab.value);
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
      await markAllAsReadMutation.mutateAsync();
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
                  :disabled="!notifications.length"
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
                  :disabled="!notifications.length"
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
                  :disabled="!notifications.length"
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
            @os-initialized="handleScrollbarsInitialized"
          >
            <VLoading v-if="isLoading" />
            <Transition v-else-if="!notifications.length" appear name="fade">
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
                class="relative box-border w-full"
                role="list"
                :style="{ height: `${totalSize}px` }"
              >
                <li
                  v-for="row in virtualRows"
                  :key="row.notification.metadata.name"
                  :ref="measureElement"
                  :data-index="row.index"
                  class="absolute left-0 top-0 w-full"
                  :class="{ 'border-t border-gray-100': row.index > 0 }"
                  :style="{ transform: `translateY(${row.start}px)` }"
                  @click="handleItemClick(row.notification, row.index, $event)"
                >
                  <NotificationListItem
                    :notification="row.notification"
                    :is-selected="
                      !isSelectMode &&
                      selectedNotificationName ===
                        row.notification.metadata.name
                    "
                    :is-select-mode="isSelectMode"
                    :is-checked="
                      selectedNames.has(row.notification.metadata.name)
                    "
                    @read="handleMarkAsRead"
                    @delete="handleDeleteNotification"
                  />
                </li>
              </ul>
            </Transition>
            <VLoading v-if="isFetchingNextPage" />
            <div v-if="loadMoreFailed" class="flex justify-center p-4">
              <VButton size="sm" @click="retryLoadMore">
                {{ $t("core.common.buttons.retry") }}
              </VButton>
            </div>
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
