import dayjs from "dayjs";
import "dayjs/locale/zh-cn";
import timezone from "dayjs/plugin/timezone";
import utc from "dayjs/plugin/utc";

dayjs.extend(timezone);
dayjs.extend(utc);

dayjs.locale("zh-cn");

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
