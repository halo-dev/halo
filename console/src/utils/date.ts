import { i18n } from "@/locales";
import dayjs from "dayjs";
import "dayjs/locale/zh-cn";
import "dayjs/locale/en";
import "dayjs/locale/zh-tw";
import timezone from "dayjs/plugin/timezone";
import utc from "dayjs/plugin/utc";
import relativeTime from "dayjs/plugin/relativeTime";
dayjs.extend(timezone);
dayjs.extend(utc);
dayjs.extend(relativeTime);
const dayjsLocales = {
  en: "en",
  zh: "zh-cn",
  "en-US": "en",
  "zh-CN": "zh-cn",
  "zh-TW": "zh-tw",
};

export function formatDatetime(
  date: string | Date | undefined | null,
  tz?: string
): string {
  if (!date) {
    return "";
  }
  return dayjs(date).tz(tz).format("YYYY-MM-DD HH:mm");
}

export function toISOString(date: string | Date | undefined | null): string {
  if (!date) {
    return "";
  }
  return dayjs(date).utc(false).toISOString();
}

export function toDatetimeLocal(
  date: string | Date | undefined | null,
  tz?: string
): string {
  if (!date) {
    return "";
  }
  // see https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/datetime-local#the_y10k_problem_often_client-side
  return dayjs(date).tz(tz).format("YYYY-MM-DDTHH:mm");
}

/**
 * Get relative time to end date
 *
 * @param date end date
 * @returns relative time to end date
 *
 * @example
 *
 * // now is 2020-12-01
 * RelativeTimeTo("2021-01-01") // in 1 month
 */
export function relativeTimeTo(date: string | Date | undefined | null) {
  dayjs.locale(dayjsLocales[i18n.global.locale.value] || dayjsLocales["zh-CN"]);

  if (!date) {
    return;
  }

  return dayjs().to(dayjs(date));
}
