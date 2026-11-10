<script lang="ts" setup>
import {
  paginate,
  ucApiClient,
  type Notification,
  type NotificationList,
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
  type InfiniteData,
  type RefetchOptions,
} from "@tanstack/vue-query";
import { useVirtualizer } from "@tanstack/vue-virtual";
import { useRouteQuery } from "@vueuse/router";
import { chunk } from "es-toolkit";
import type { OverlayScrollbars } from "overlayscrollbars";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";
import {
  computed,
  onUnmounted,
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

const PAGE_SIZE = 50;
const DELETION_POLL_INTERVAL = 1000;

const queryClient = useQueryClient();
const { t } = useI18n();
const { currentUser } = stores.currentUser();

const activeTab = useRouteQuery("tab", "unread");

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
  queryFn: async ({ pageParam = 1, queryKey }) => {
    const { data } =
      await ucApiClient.notification.notification.listUserNotifications({
        username: currentUser?.user.metadata.name as string,
        page: pageParam,
        size: PAGE_SIZE,
        fieldSelector: [`spec.unread=${queryKey[1] === "unread"}`],
      });
    return data;
  },
  getNextPageParam: (lastPage) =>
    lastPage.hasNext ? lastPage.page + 1 : undefined,
  cacheTime: 0,
});

// The `refetch` exposed by useInfiniteQuery supports `refetchPage` at runtime
// (InfiniteQueryObserver.refetch), but v4 does not include it in the option
// type.
type RefetchOptionsWithPages = RefetchOptions & {
  refetchPage?: (
    page: NotificationList,
    index: number,
    allPages: NotificationList[]
  ) => boolean;
};

function refetchPages(options: RefetchOptionsWithPages) {
  return refetch(options);
}

// Notifications marked as read without refetching the list. Offset pagination
// shifts after they are removed from the unread set, so the loaded pages are
// repaired before the next page is fetched.
const locallyReadNames = reactive(new Set<string>());
const markingReadNames = reactive(new Set<string>());

const notifications = computed(() => {
  const items =
    notificationPages.value?.pages.flatMap((page) => page.items) ?? [];
  // Offset pagination can return the same notification on two pages when new
  // notifications arrive between page requests, so drop the duplicates.
  const seen = new Set<string>();
  return items
    .filter((item) => {
      if (seen.has(item.metadata.name)) {
        return false;
      }
      seen.add(item.metadata.name);
      return true;
    })
    .map((item) =>
      // Keep the read state stable when an in-flight page fetch overwrites the
      // optimistic cache update.
      locallyReadNames.has(item.metadata.name) && item.spec?.unread
        ? { ...item, spec: { ...item.spec, unread: false } }
        : item
    );
});

const selectedNotificationName = useRouteQuery<string | undefined>("name");

const selectedNotificationFromList = computed(() => {
  return notifications.value.find(
    (item) => item.metadata.name === selectedNotificationName.value
  );
});

// The selected notification may not be loaded yet (deep link) or may drop out
// of the loaded unread pages after it is marked as read, so fall back to
// fetching it by name.
const { data: selectedNotificationById } = useQuery({
  queryKey: ["user-notification", selectedNotificationName],
  enabled: computed(
    () =>
      !!selectedNotificationName.value && !selectedNotificationFromList.value
  ),
  queryFn: async ({ queryKey }) => {
    const { data } =
      await ucApiClient.notification.notification.listUserNotifications({
        username: currentUser?.user.metadata.name as string,
        page: 1,
        size: 1,
        fieldSelector: [`metadata.name=${queryKey[1]}`],
      });
    return data.items.find((item) => item.metadata.name === queryKey[1]);
  },
});

const selectedNotification = computed(
  () => selectedNotificationFromList.value ?? selectedNotificationById.value
);

function updateCachedNotificationReadState(name: string) {
  queryClient.setQueriesData<InfiniteData<NotificationList>>(
    { queryKey: ["user-notifications"] },
    (data) => {
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

const { mutate: markSelectedAsRead } = useMutation({
  mutationKey: ["notification-mark-as-read"],
  mutationFn: async (notification: Notification) => {
    await ucApiClient.notification.notification.markNotificationAsRead({
      name: notification.metadata.name,
      username: currentUser?.user.metadata.name as string,
    });
  },
  onSuccess(_, notification) {
    markingReadNames.delete(notification.metadata.name);
    updateCachedNotificationReadState(notification.metadata.name);
    if (activeTab.value === "unread") {
      locallyReadNames.add(notification.metadata.name);
    }
  },
  onError(_, notification) {
    markingReadNames.delete(notification.metadata.name);
  },
});

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
      markSelectedAsRead(notification);
    }
  },
  { immediate: true }
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

const markingAsReadCount = useIsMutating({
  mutationKey: ["notification-mark-as-read"],
});

const isLoadingMore = ref(false);
const loadMoreFailed = ref(false);

/**
 * After a notification is marked as read without refetching, the server-side
 * unread set shrinks and later pages shift by one. Refetch the loaded pages so
 * the next page starts at the right offset.
 */
function confirmLocallyRead(
  pages: NotificationList[] | undefined,
  names: string[]
) {
  if (!pages) {
    return;
  }

  const stillUnread = new Set<string>();
  for (const page of pages) {
    for (const item of page.items) {
      if (item.spec?.unread) {
        stillUnread.add(item.metadata.name);
      }
    }
  }

  for (const name of names) {
    if (!stillUnread.has(name)) {
      locallyReadNames.delete(name);
    }
  }
}

/**
 * Refetch the loaded pages until the server reflects the notifications that
 * were marked as read without refetching the list. Returns false when the
 * repair could not be completed, in which case the caller should stop for now.
 */
async function repairLoadedPages(tab: string) {
  for (let attempt = 0; attempt < 3; attempt++) {
    if (locallyReadNames.size === 0) {
      return true;
    }

    const pendingNames = [...locallyReadNames];
    const result = await refetchPages({ cancelRefetch: false });

    if (activeTab.value !== tab) {
      return false;
    }

    if (result.isError) {
      loadMoreFailed.value = true;
      return false;
    }

    if (result.isFetching) {
      // Another fetch superseded the repair; the watcher retries later.
      return false;
    }

    confirmLocallyRead(result.data?.pages, pendingNames);

    if (pendingNames.every((name) => !locallyReadNames.has(name))) {
      if (locallyReadNames.size === 0) {
        return true;
      }
      // Notifications marked as read while the repair was running need
      // another round.
      continue;
    }
  }

  loadMoreFailed.value = true;
  return false;
}

async function loadNextPage() {
  if (isLoadingMore.value || isFetching.value) {
    return;
  }

  isLoadingMore.value = true;

  try {
    const tab = activeTab.value;

    if (tab === "unread" && locallyReadNames.size > 0) {
      if (!(await repairLoadedPages(tab))) {
        return;
      }
    }

    if (isFetching.value || markingAsReadCount.value > 0) {
      return;
    }

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
    markingAsReadCount,
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
      markingAsReadCount.value > 0 ||
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

// ---- Deletion polling ----
const hasDeletingNotifications = computed(
  () =>
    notificationPages.value?.pages.some((page) =>
      page.items.some((item) => item.metadata.deletionTimestamp !== undefined)
    ) ?? false
);

let deletionPollTimer: ReturnType<typeof setTimeout> | undefined;

function stopDeletionPolling() {
  if (deletionPollTimer !== undefined) {
    clearTimeout(deletionPollTimer);
    deletionPollTimer = undefined;
  }
}

function scheduleDeletionPolling() {
  stopDeletionPolling();

  if (!hasDeletingNotifications.value) {
    return;
  }

  deletionPollTimer = setTimeout(async () => {
    deletionPollTimer = undefined;

    if (!hasDeletingNotifications.value) {
      return;
    }

    // Refetch the first page that still contains a deleting notification and
    // every page after it, so the page boundaries stay aligned after the item
    // is removed from the server-side list.
    await refetchPages({
      cancelRefetch: false,
      refetchPage: (_page, index, allPages) =>
        allPages
          .slice(0, index + 1)
          .some((page) =>
            page.items.some(
              (item) => item.metadata.deletionTimestamp !== undefined
            )
          ),
    });

    scheduleDeletionPolling();
  }, DELETION_POLL_INTERVAL);
}

watch(
  hasDeletingNotifications,
  (hasDeleting) => {
    if (hasDeleting) {
      scheduleDeletionPolling();
    } else {
      stopDeletionPolling();
    }
  },
  { immediate: true }
);

onUnmounted(stopDeletionPolling);
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
  locallyReadNames.clear();
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

/**
 * Fetch all notifications of the current tab. Used by batch operations
 * ("delete all" / "mark all as read") so they are not limited to the pages
 * that have been scrolled into view.
 */
async function listAllNotifications() {
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
}

function handleDeleteNotifications() {
  Dialog.warning({
    title: t("core.uc_notification.operations.delete_all.title"),
    description: t("core.uc_notification.operations.delete_all.description"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    confirmType: "danger",
    onConfirm: async () => {
      if (!currentUser) {
        throw new Error("Current user is not found");
      }

      const allNotifications = await listAllNotifications();

      if (allNotifications.length === 0) {
        return;
      }

      const notificationChunks = chunk(allNotifications, 5);

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
      if (!currentUser) {
        throw new Error("Current user is not found");
      }

      const allNotifications = await listAllNotifications();

      if (allNotifications.length === 0) {
        return;
      }

      const names = allNotifications.map(
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
