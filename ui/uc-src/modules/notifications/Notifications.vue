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
import {
  useInfiniteQuery,
  useIsMutating,
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/vue-query";
import { useVirtualizer } from "@tanstack/vue-virtual";
import { useRouteQuery } from "@vueuse/router";
import { chunk } from "es-toolkit";
import type { OverlayScrollbars } from "overlayscrollbars";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";
import { computed, onUnmounted, ref, shallowRef, watch } from "vue";
import { useI18n } from "vue-i18n";
import RiCheckDoubleLine from "~icons/ri/check-double-line";
import RiCheckboxMultipleFill from "~icons/ri/checkbox-multiple-fill";
import RiCheckboxMultipleLine from "~icons/ri/checkbox-multiple-line";
import NotificationContent from "./components/NotificationContent.vue";
import NotificationListItem from "./components/NotificationListItem.vue";
import {
  clearLocalNotificationState,
  locallyDeletedNotificationNames,
  locallyReadNotificationNames,
  locallyRemovedUnreadNames,
  updateCachedNotificationReadState,
} from "./local-notification-state";

const queryClient = useQueryClient();
const { t } = useI18n();
const { currentUser } = stores.currentUser();

const activeTab = useRouteQuery("tab", "unread");

const PAGE_SIZE = 50;

// Markers can outlive an unmount when a mutation settles late; start each
// visit with a clean slate and repair only from what happens in this visit.
clearLocalNotificationState();

const {
  data: notificationPages,
  isLoading,
  isError,
  refetch,
  isFetching,
  fetchNextPage,
  hasNextPage,
  isFetchingNextPage,
} = useInfiniteQuery({
  queryKey: ["user-notifications", activeTab],
  queryFn: async ({ pageParam = 1, queryKey }) => {
    // Read the tab from the query key: a refetch of an old key may still be
    // running after the route query has already switched to the other tab.
    const tab = queryKey[1];
    const { data } =
      await ucApiClient.notification.notification.listUserNotifications({
        username: currentUser?.user.metadata.name as string,
        fieldSelector: [`spec.unread=${tab === "unread"}`],
        page: pageParam,
        size: PAGE_SIZE,
      });

    return data;
  },
  getNextPageParam: (lastPage) => {
    return lastPage.hasNext ? lastPage.page + 1 : undefined;
  },
  cacheTime: 0,
  refetchInterval(data) {
    const hasDeletingNotifications = data?.pages.some((page) =>
      page.items.some((item) => item.metadata.deletionTimestamp !== undefined)
    );

    return hasDeletingNotifications ? 1000 : false;
  },
});

const notifications = computed(() => {
  const items =
    notificationPages.value?.pages.flatMap((page) => page.items) ?? [];
  const seen = new Set<string>();
  const isUnreadTab = activeTab.value === "unread";

  // Local reads win over the cached pages: a refetch that was already in
  // flight when a notification was read may still return it as unread.
  return items.reduce<Notification[]>((result, item) => {
    const name = item.metadata.name;
    if (seen.has(name)) {
      return result;
    }
    seen.add(name);

    if (isUnreadTab && locallyRemovedUnreadNames.has(name)) {
      return result;
    }

    if (item.spec?.unread && locallyReadNotificationNames.has(name)) {
      result.push({ ...item, spec: { ...item.spec, unread: false } });
      return result;
    }

    result.push(item);
    return result;
  }, []);
});

const markingAsReadCount = useIsMutating({
  mutationKey: ["notification-mark-as-read"],
});

// ---- Infinite scroll ----
const scrollViewport = shallowRef<HTMLElement | null>(null);

function handleScrollbarsInitialized(instance: OverlayScrollbars) {
  scrollViewport.value = instance.elements().viewport;
}

// Marked-as-read notifications shrink the server-side unread set without
// refreshing the loaded pages, so loading the next page by offset could skip
// an item. The signal lives outside the query cache because a late page
// response can overwrite the cached read state.
function needsPageRepair(tab: string) {
  return (
    tab === "unread" &&
    activeTab.value === tab &&
    locallyReadNotificationNames.size > 0
  );
}

// A marker may only be dropped once the server confirms the read, i.e. the
// refetched pages no longer return that notification as unread. Dropping it
// earlier would let a stale refetch render the notification as unread again.
function confirmLocallyRead() {
  if (activeTab.value !== "unread") {
    return;
  }

  const stillUnread = new Set<string>();
  for (const page of notificationPages.value?.pages ?? []) {
    for (const item of page.items) {
      if (item.spec?.unread) {
        stillUnread.add(item.metadata.name);
      }
    }
  }

  for (const name of [...locallyReadNotificationNames]) {
    if (stillUnread.has(name)) {
      continue;
    }

    locallyReadNotificationNames.delete(name);
    locallyRemovedUnreadNames.delete(name);
  }
}

async function repairLoadedPages(tab: string) {
  for (let attempt = 0; attempt < 3; attempt++) {
    if (!needsPageRepair(tab)) {
      return true;
    }

    const readCount = locallyReadNotificationNames.size;
    await refetch({ cancelRefetch: false });

    if (activeTab.value !== tab || isError.value) {
      return false;
    }

    if (locallyReadNotificationNames.size !== readCount) {
      continue;
    }

    // A read whose response has not arrived yet will also shift the
    // server-side offsets; keep the marker and let the mutation watcher
    // schedule another repair.
    if (markingAsReadCount.value > 0) {
      return false;
    }

    confirmLocallyRead();

    if (locallyReadNotificationNames.size === 0) {
      return true;
    }
  }

  return false;
}

async function waitForMarkAsReadMutations() {
  if (markingAsReadCount.value === 0) {
    return;
  }

  await new Promise<void>((resolve) => {
    const stop = watch(markingAsReadCount, (count) => {
      if (count === 0) {
        stop();
        clearTimeout(timer);
        resolve();
      }
    });
    // Never block infinite scrolling on a hung request; the dirty marker
    // still triggers another repair when the request finally settles.
    const timer = setTimeout(() => {
      stop();
      resolve();
    }, 10_000);
  });
}

let isLoadingMore = false;

async function loadMore() {
  if (
    isLoadingMore ||
    isFetchingNextPage.value ||
    isFetching.value ||
    markingAsReadCount.value > 0
  ) {
    return;
  }

  const tab = activeTab.value;

  // Repairing is independent of pagination: a read can leave a gap on the
  // last loaded page, where there is no next page left to fetch.
  if (!hasNextPage?.value && !needsPageRepair(tab)) {
    return;
  }

  isLoadingMore = true;

  try {
    if (!(await repairLoadedPages(tab))) {
      return;
    }

    if (activeTab.value !== tab) {
      return;
    }

    // Repairing may have consumed the last page.
    if (!hasNextPage?.value) {
      return;
    }

    if (isFetching.value || markingAsReadCount.value > 0) {
      return;
    }

    await fetchNextPage({ cancelRefetch: false });
    if (activeTab.value !== tab) {
      return;
    }

    // A notification read while the page was loading may have shifted the
    // server-side offsets or overwritten its cached read state.
    await waitForMarkAsReadMutations();
    await repairLoadedPages(tab);
  } catch {
    // The query state keeps the error; the footer offers a retry.
  } finally {
    isLoadingMore = false;
  }
}

const virtualizerOptions = computed(() => ({
  count: notifications.value.length,
  getScrollElement: () => scrollViewport.value,
  estimateSize: () => 108,
  getItemKey: (index: number) =>
    notifications.value[index]?.metadata.name ?? index,
  overscan: 5,
}));

const virtualizer = useVirtualizer(virtualizerOptions);

const virtualItems = computed(() => virtualizer.value.getVirtualItems());
const virtualListSize = computed(() => virtualizer.value.getTotalSize());

const virtualRows = computed(() => {
  return virtualItems.value.map((virtualItem) => ({
    index: virtualItem.index,
    start: virtualItem.start,
    notification: notifications.value[virtualItem.index],
  }));
});

function measureRow(element: unknown) {
  virtualizer.value.measureElement(element instanceof Element ? element : null);
}

watch(
  [
    () => virtualItems.value[virtualItems.value.length - 1]?.index,
    isFetching,
    markingAsReadCount,
  ],
  ([lastIndex, fetching, markingAsRead]) => {
    if (
      !fetching &&
      markingAsRead === 0 &&
      !isError.value &&
      lastIndex !== undefined &&
      lastIndex >= notifications.value.length - 1
    ) {
      loadMore();
    }
  },
  { flush: "post" }
);

const selectedNotificationName = useRouteQuery<string | undefined>("name");

// Keep the opened notification visible when a refetch drops it from the list,
// e.g. after it was marked as read and the unread pages were refreshed.
const selectedNotificationSnapshot = shallowRef<Notification>();

watch(selectedNotificationName, (name) => {
  if (selectedNotificationSnapshot.value?.metadata.name !== name) {
    selectedNotificationSnapshot.value = undefined;
  }
});

const loadedSelectedNotification = computed(() => {
  return notifications.value.find(
    (item) => item.metadata.name === selectedNotificationName.value
  );
});

// Deep links can point to a notification outside the loaded pages.
const { data: fetchedSelectedNotification } = useQuery({
  queryKey: ["user-notification", selectedNotificationName],
  queryFn: async ({ queryKey }) => {
    const name = queryKey[1];
    const { data } =
      await ucApiClient.notification.notification.listUserNotifications({
        username: currentUser?.user.metadata.name as string,
        fieldSelector: [`metadata.name=${name}`],
        page: 1,
        size: 1,
      });

    return data.items[0];
  },
  enabled: computed(
    () =>
      !!currentUser?.user.metadata.name &&
      !!selectedNotificationName.value &&
      !loadedSelectedNotification.value &&
      selectedNotificationSnapshot.value?.metadata.name !==
        selectedNotificationName.value
  ),
  cacheTime: 0,
});

// A deep-linked notification has no row to trigger the passive mark-as-read.
const { mutate: markFetchedNotificationAsRead } = useMutation({
  mutationKey: ["notification-mark-as-read"],
  mutationFn: async (notification: Notification) => {
    const { data } =
      await ucApiClient.notification.notification.markNotificationAsRead({
        name: notification.metadata.name,
        username: currentUser?.user.metadata.name as string,
      });

    return data;
  },
  onSuccess(_data, notification) {
    locallyReadNotificationNames.add(notification.metadata.name);
    updateCachedNotificationReadState(queryClient, notification.metadata.name);
  },
});

watch(
  fetchedSelectedNotification,
  (notification) => {
    if (notification?.spec?.unread) {
      markFetchedNotificationAsRead(notification);
    }
  },
  { immediate: true }
);

const selectedNotification = computed(() => {
  if (loadedSelectedNotification.value) {
    return loadedSelectedNotification.value;
  }

  if (fetchedSelectedNotification.value) {
    return fetchedSelectedNotification.value;
  }

  if (
    selectedNotificationSnapshot.value?.metadata.name ===
    selectedNotificationName.value
  ) {
    return selectedNotificationSnapshot.value;
  }

  return undefined;
});

function clearSelectedNotification() {
  selectedNotificationSnapshot.value = undefined;
  selectedNotificationName.value = undefined;
}

watch(
  () => locallyDeletedNotificationNames.size,
  () => {
    if (
      selectedNotificationName.value &&
      locallyDeletedNotificationNames.has(selectedNotificationName.value)
    ) {
      clearSelectedNotification();
    }
  }
);

// ---- Batch selection state ----
const isSelectMode = ref(false);
const selectedNames = ref<Set<string>>(new Set<string>());
const lastSelectedIndex = ref<number>(-1);
const selectAllActive = ref(false);
const isSelectingAll = ref(false);
let selectAllRequestId = 0;

const isAllSelected = computed(
  () => selectAllActive.value && selectedNames.value.size > 0
);

// Exit select mode and clear selections when switching tabs. The read/deleted
// markers outlive the tab switch: a page fetched before the switch can still
// resolve afterwards, and its repair marker must survive.
watch(activeTab, () => {
  isSelectMode.value = false;
  selectedNames.value.clear();
  lastSelectedIndex.value = -1;
  selectAllActive.value = false;
  selectAllRequestId++;
});

onUnmounted(clearLocalNotificationState);

// Auto-exit select mode when the list becomes empty (e.g. after deleting all).
watch(notifications, (newVal) => {
  if (isSelectMode.value && newVal !== undefined && newVal.length === 0) {
    isSelectMode.value = false;
    selectedNames.value.clear();
    lastSelectedIndex.value = -1;
    selectAllActive.value = false;
  }
});

function toggleSelectMode() {
  isSelectMode.value = !isSelectMode.value;
  if (!isSelectMode.value) {
    selectedNames.value.clear();
    lastSelectedIndex.value = -1;
    selectAllActive.value = false;
    selectAllRequestId++;
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
    // A soft-deleted notification can stay in the list until the next
    // refetch; reopening it would revive a snapshot the delete watcher can
    // no longer clear.
    if (locallyDeletedNotificationNames.has(notification.metadata.name)) {
      return;
    }

    selectedNotificationSnapshot.value = notification;
    selectedNotificationName.value = notification.metadata.name;
    return;
  }

  const name = notification.metadata.name;
  selectAllActive.value = false;

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

async function handleSelectAll() {
  if (isAllSelected.value) {
    selectedNames.value.clear();
    selectAllActive.value = false;
    selectAllRequestId++;
    return;
  }

  if (!currentUser || isSelectingAll.value) {
    return;
  }

  const tab = activeTab.value;
  const requestId = ++selectAllRequestId;
  isSelectingAll.value = true;

  try {
    const names = await listAllNotificationNames(
      currentUser.user.metadata.name
    );

    if (
      requestId !== selectAllRequestId ||
      !isSelectMode.value ||
      activeTab.value !== tab
    ) {
      return;
    }

    selectedNames.value = new Set(names);
    selectAllActive.value = true;
  } catch {
    Toast.error(t("core.common.toast.operation_failed"));
  } finally {
    isSelectingAll.value = false;
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
      selectAllRequestId++;

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
      selectAllActive.value = false;

      if (
        selectedNotificationName.value &&
        namesToDelete.includes(selectedNotificationName.value)
      ) {
        clearSelectedNotification();
      }

      await queryClient.invalidateQueries({ queryKey: ["user-notifications"] });
      Toast.success(t("core.common.toast.delete_success"));
    },
  });
}
// ---- End batch selection ----

/**
 * List all notification names of the current tab for batch operations.
 * The list itself is paginated, but batch operations still apply to every notification.
 */
async function listAllNotificationNames(username: string) {
  const notifications = await paginate<
    NotificationV1alpha1UcApiListUserNotificationsRequest,
    Notification
  >(
    (params) =>
      ucApiClient.notification.notification.listUserNotifications(params),
    {
      username,
      fieldSelector: [`spec.unread=${activeTab.value === "unread"}`],
      size: 1000,
    }
  );

  return notifications.map((notification) => notification.metadata.name);
}

function handleDeleteNotifications() {
  Dialog.warning({
    title: t("core.uc_notification.operations.delete_all.title"),
    description: t("core.uc_notification.operations.delete_all.description"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    confirmType: "danger",
    onConfirm: async () => {
      if (!notifications.value.length) {
        return;
      }

      if (!currentUser) {
        throw new Error("Current user is not found");
      }

      const names = await listAllNotificationNames(
        currentUser.user.metadata.name
      );
      const notificationChunks = chunk(names, 5);

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

      if (
        selectedNotificationName.value &&
        names.includes(selectedNotificationName.value)
      ) {
        clearSelectedNotification();
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
      if (!notifications.value.length) {
        return;
      }

      if (!currentUser) {
        throw new Error("Current user is not found");
      }

      const names = await listAllNotificationNames(
        currentUser.user.metadata.name
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
                  :disabled="isSelectingAll"
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
              <div>
                <ul
                  class="relative w-full"
                  role="list"
                  :style="{ height: `${virtualListSize}px` }"
                >
                  <li
                    v-for="row in virtualRows"
                    :key="row.notification.metadata.name"
                    :ref="measureRow"
                    :data-index="row.index"
                    class="absolute left-0 top-0 w-full"
                    :class="{
                      'border-t border-gray-100': row.index > 0,
                    }"
                    :style="{ transform: `translateY(${row.start}px)` }"
                    @click="
                      handleItemClick(row.notification, row.index, $event)
                    "
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
                    />
                  </li>
                </ul>
                <div
                  v-if="hasNextPage"
                  class="flex items-center justify-center py-2"
                >
                  <VLoading v-if="isFetchingNextPage" class="!py-0" />
                  <button
                    v-else-if="isError"
                    type="button"
                    class="text-sm text-gray-600 hover:text-gray-900"
                    @click="loadMore"
                  >
                    {{ $t("core.common.buttons.retry") }}
                  </button>
                </div>
              </div>
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
