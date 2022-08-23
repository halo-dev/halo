import dayjs from "dayjs";
import "dayjs/locale/zh-cn";
import timezone from "dayjs/plugin/timezone";

dayjs.extend(timezone);

dayjs.locale("zh-cn");

export function formatDatetime(date: string | Date | undefined | null): string {
  if (!date) {
    return "";
  }
  return dayjs(date).format("YYYY-MM-DD HH:mm");
}
