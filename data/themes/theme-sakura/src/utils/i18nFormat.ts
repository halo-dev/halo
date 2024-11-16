import i18next from "i18next";

/**
 * 提供给 i18n 所使用的模板工具类
 *
 * @see https://www.i18next.com/translation-function/formatting#additional-options
 * @see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Intl
 */
export class I18nFormat {
  /**
   * 对日期字符串进行国际化处理
   *
   * @param time 需要格式化的日期
   * @param lng locale
   * @param options 自定义日期时间格式化方法的返回值参数
   * @param separator 日期之间的分隔符
   * @returns
   *
   * @see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Intl/DateTimeFormat
   */
  static DateTimeFormat(
    time: string,
    lng: string | undefined = i18next.language,
    options?: Intl.DateTimeFormatOptions,
    separator?: string
  ) {
    const date = new Date(time);
    if (separator) {
      return new Intl.DateTimeFormat(lng, options).format(date).replace(/\//g, separator);
    }
    return new Intl.DateTimeFormat(lng, options).format(date);
  }

  /**
   * 将时间戳转换为相对时间
   * 例如： 60 秒前，1 分钟前，1 小时前，1 天前，1 月前，1 年前
   *
   * @param millisecond 毫秒
   * @returns
   */
  static RelativeTimeFormat(timestamp: number, lng: string | undefined = i18next.language) {
    const currentTime = new Date().getTime();
    const betweenTime = currentTime - timestamp;
    const diffInSeconds = Math.round(betweenTime / 1000);

    const rtf = new Intl.RelativeTimeFormat(lng, {
      style: "long",
    });

    if (diffInSeconds < 60) {
      return rtf.format(-diffInSeconds, "second");
    } else if (diffInSeconds < 3600) {
      return rtf.format(-Math.floor(diffInSeconds / 60), "minute");
    } else if (diffInSeconds < 86400) {
      return rtf.format(-Math.floor(diffInSeconds / 3600), "hour");
    } else if (diffInSeconds < 2592000) {
      return rtf.format(-Math.floor(diffInSeconds / 86400), "day");
    } else if (diffInSeconds < 31104000) {
      return rtf.format(-Math.floor(diffInSeconds / 2592000), "month");
    } else {
      return rtf.format(-Math.floor(diffInSeconds / 31104000), "year");
    }
  }

  /**
   * 将秒转换为时间字符串
   *
   * @param second 秒
   * @returns {string} 时间字符串
   */
  static secondToTimeString(second: number) {
    let minutes = Math.floor(second / 60);
    let seconds = second % 60;

    let hours = Math.floor(minutes / 60);
    minutes %= 60;

    let days = Math.floor(hours / 24);
    hours %= 24;

    let result = "";

    if (days > 0) {
      result += i18next.t("common.days", { count: days, defaultValue: "{{ count }} 天" }) + " ";
    }

    if (hours > 0) {
      result += i18next.t("common.hours", { count: hours, defaultValue: "{{ count }} 小时" }) + " ";
    }

    if (minutes > 0) {
      result += i18next.t("common.minutes", { count: minutes, defaultValue: "{{ count }} 分钟" }) + " ";
    }

    if (seconds > 0) {
      result += i18next.t("common.seconds", { count: seconds, defaultValue: "{{ count }} 秒" }) + " ";
    }

    return result.trim();
  }
}
