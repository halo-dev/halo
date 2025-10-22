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
  format(date: string | Date | undefined | null, format?: string): string {
    if (!date) {
      return "";
    }
    return dayjs(date).format(format || DEFAULT_FORMAT);
  }

  toISOString(date: string | Date | undefined | null): string {
    if (!date) {
      return "";
    }
    return dayjs(date).utc(false).toISOString();
  }

  toDatetimeLocal(date: string | Date | undefined | null): string {
    if (!date) {
      return "";
    }
    // see https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/datetime-local#the_y10k_problem_often_client-side
    return dayjs(date).format("YYYY-MM-DDTHH:mm");
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
  timeAgo(date: string | Date | undefined | null): string | undefined {
    if (!date) {
      return;
    }

    return dayjs().to(dayjs(date));
  }

  setLocale(locale: string): void {
    dayjs.locale(dayjsLocales[locale] || dayjsLocales["en"]);
  }
}
