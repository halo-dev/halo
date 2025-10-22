import dayjs from "dayjs";
import "dayjs/locale/en";
import "dayjs/locale/zh-cn";
import "dayjs/locale/zh-tw";
import relativeTime from "dayjs/plugin/relativeTime";
import timezone from "dayjs/plugin/timezone";
import utc from "dayjs/plugin/utc";

dayjs.extend(timezone);
dayjs.extend(utc);
dayjs.extend(relativeTime);

const DEFAULT_FORMAT = "YYYY-MM-DD HH:mm";

const dayjsLocales: Record<string, string> = {
  en: "en",
  zh: "zh-cn",
  "en-US": "en",
  "zh-CN": "zh-cn",
  "zh-TW": "zh-tw",
};

export class DateUtils {
  /**
   * Formats a date to a string according to the specified format
   *
   * @param date - The date to format (string, Date object, or null/undefined)
   * @param format - The format string (defaults to "YYYY-MM-DD HH:mm")
   * @returns The formatted date string, or empty string if date is null/undefined
   *
   * @example
   * ```ts
   * import { utils } from "@halo-dev/console-shared"
   * utils.date.format(new Date()); // "2025-10-22 14:30"
   * utils.date.format("2025-10-22", "YYYY/MM/DD"); // "2025/10/22"
   * ```
   */
  format(date: string | Date | undefined | null, format?: string): string {
    if (!date) {
      return "";
    }
    return dayjs(date).format(format || DEFAULT_FORMAT);
  }

  /**
   * Converts a date to ISO 8601 format string
   *
   * @param date - The date to convert (string, Date object, or null/undefined)
   * @returns The ISO 8601 formatted date string, or empty string if date is null/undefined
   *
   * @example
   * ```ts
   * import { utils } from "@halo-dev/console-shared"
   * utils.date.toISOString(new Date("2025-10-22")); // "2025-10-22T00:00:00.000Z"
   * ```
   */
  toISOString(date: string | Date | undefined | null): string {
    if (!date) {
      return "";
    }
    return dayjs(date).utc(false).toISOString();
  }

  /**
   * Converts a date to HTML5 datetime-local input format
   *
   * @param date - The date to convert (string, Date object, or null/undefined)
   * @returns The datetime-local formatted string (YYYY-MM-DDTHH:mm), or empty string if date is null/undefined
   *
   * @see https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/datetime-local#the_y10k_problem_often_client-side
   *
   * @example
   * ```ts
   * import { utils } from "@halo-dev/console-shared"
   * utils.date.toDatetimeLocal(new Date("2025-10-22 14:30")); // "2025-10-22T14:30"
   * ```
   */
  toDatetimeLocal(date: string | Date | undefined | null): string {
    if (!date) {
      return "";
    }
    // see https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/datetime-local#the_y10k_problem_often_client-side
    return dayjs(date).format("YYYY-MM-DDTHH:mm");
  }

  /**
   * Gets the relative time from now to the specified date
   *
   * @param date - The target date (string, Date object, or null/undefined)
   * @returns A human-readable relative time string (e.g., "in 2 hours", "3 days ago"), or undefined if date is null/undefined
   *
   * @example
   * ```ts
   * import { utils } from "@halo-dev/console-shared"
   * // Assuming now is 2025-10-22
   * utils.date.timeAgo("2025-10-23"); // "in a day"
   * utils.date.timeAgo("2025-10-21"); // "a day ago"
   * utils.date.timeAgo("2025-11-22"); // "in a month"
   * ```
   */
  timeAgo(date: string | Date | undefined | null): string | undefined {
    if (!date) {
      return;
    }

    return dayjs().to(dayjs(date));
  }

  /**
   * Sets the locale for date formatting
   *
   * @param locale - The locale code (e.g., "en", "zh", "en-US", "zh-CN", "zh-TW")
   *
   * @remarks
   * Supported locales:
   * - "en" or "en-US" → English
   * - "zh" or "zh-CN" → Simplified Chinese
   * - "zh-TW" → Traditional Chinese
   *
   * Defaults to English if the locale is not supported.
   *
   * @example
   * ```ts
   * import { utils } from "@halo-dev/console-shared"
   * utils.date.setLocale("zh-CN");
   * utils.date.timeAgo("2025-10-21"); // "1 天前"
   * utils.date.setLocale("en");
   * utils.date.timeAgo("2025-10-21"); // "a day ago"
   * ```
   */
  setLocale(locale: string): void {
    dayjs.locale(dayjsLocales[locale] || dayjsLocales["en"]);
  }
}
