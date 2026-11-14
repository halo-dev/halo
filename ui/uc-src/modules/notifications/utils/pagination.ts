export const NOTIFICATION_PAGE_SIZE = 50;

export interface NotificationPageOffset {
  nextOffset: number;
  total: number;
  removalsAtFetch: number;
}

/**
 * Compute the next aligned page offset.
 *
 * Page keys are aligned to {@link NOTIFICATION_PAGE_SIZE}. When notifications
 * are marked as read or deleted locally, the server-side list shrinks and the
 * next offset must move backwards by the same amount. Rounding down can only
 * overlap pages (which are deduplicated); rounding up would skip the
 * notification at the page boundary.
 */
export function getNextNotificationPageOffset(
  lastPage: NotificationPageOffset,
  removedCount: number,
  pageSize = NOTIFICATION_PAGE_SIZE
): number | undefined {
  const logicalOffset =
    lastPage.nextOffset - (removedCount - lastPage.removalsAtFetch);

  if (logicalOffset >= lastPage.total) {
    return undefined;
  }

  return Math.max(0, Math.floor(logicalOffset / pageSize) * pageSize);
}
