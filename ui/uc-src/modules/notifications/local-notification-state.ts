import type { NotificationList } from "@halo-dev/api-client";
import type { InfiniteData, QueryClient } from "@tanstack/vue-query";
import { reactive } from "vue";

/**
 * Notifications marked as read during this visit.
 *
 * Kept outside the paginated query cache so that an in-flight page response
 * cannot erase the signal that the loaded pages need to be refreshed.
 */
export const locallyReadNotificationNames = reactive(new Set<string>());

/**
 * Notifications read through the explicit "mark as read" action during this
 * visit. They are hidden from the unread list until a refetch confirms that
 * the server no longer returns them as unread.
 */
export const locallyRemovedUnreadNames = reactive(new Set<string>());

/** Notifications deleted during this visit, independent of virtual row lifetime. */
export const locallyDeletedNotificationNames = reactive(new Set<string>());

export function clearLocalNotificationState() {
  locallyReadNotificationNames.clear();
  locallyRemovedUnreadNames.clear();
  locallyDeletedNotificationNames.clear();
}

/**
 * Reflect a read notification in the cached notification pages, so a virtual
 * row that was unmounted while the mutation settled does not render it as
 * unread when it scrolls back into view.
 */
export function updateCachedNotificationReadState(
  queryClient: QueryClient,
  name: string
) {
  queryClient.setQueriesData<InfiniteData<NotificationList>>(
    {
      predicate: (query) =>
        Array.isArray(query.queryKey) &&
        query.queryKey[0] === "user-notifications" &&
        query.queryKey.length === 2,
    },
    (data) => {
      if (!data) {
        return data;
      }

      return {
        ...data,
        pages: data.pages.map((page) => ({
          ...page,
          items: page.items.map((item) => {
            if (item.metadata.name !== name || !item.spec?.unread) {
              return item;
            }

            return { ...item, spec: { ...item.spec, unread: false } };
          }),
        })),
      };
    }
  );
}
