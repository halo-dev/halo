/**
 * Date values are strings to keep the public API independent from
 * `@internationalized/date`:
 * - date: `YYYY-MM-DD`
 * - date time: `YYYY-MM-DDTHH:mm`
 */
export interface DateRangeValue {
  start?: string;
  end?: string;
}

export type DatePickerWeekStart = 0 | 1 | 2 | 3 | 4 | 5 | 6;

export interface DatePickerPreset<T> {
  /**
   * Text displayed in the preset list.
   */
  label: string;
  /**
   * Preset value. Functions are evaluated when the preset is clicked so that
   * relative ranges such as "today" stay accurate.
   */
  value: T | (() => T);
}

export interface DatePickerCommonProps {
  /**
   * Locale used by the calendar, for example `zh-CN`. Defaults to `en`.
   */
  locale?: string;
  /**
   * First day of the week, `0` is Sunday.
   */
  weekStartsOn?: DatePickerWeekStart;
  /**
   * Number of months rendered side by side. Defaults to `1`, range panels
   * default to `2`.
   */
  numberOfMonths?: number;
  /**
   * Always render six weeks so the panel height stays stable.
   */
  fixedWeeks?: boolean;
  /**
   * Minimum selectable date, `YYYY-MM-DD`.
   */
  minValue?: string;
  /**
   * Maximum selectable date, `YYYY-MM-DD`.
   */
  maxValue?: string;
  /**
   * Called for every rendered day. Receives `YYYY-MM-DD`.
   */
  isDateDisabled?: (date: string) => boolean;
  /**
   * Month displayed when there is no value. Receives `YYYY-MM-DD` or a date
   * time string.
   */
  defaultPickerValue?: string;
  /**
   * Accessible label of the calendar grid.
   */
  calendarLabel?: string;
  disabled?: boolean;
  readonly?: boolean;
}

export interface DateRangePanelProps {
  /**
   * Maximum number of days that can be selected.
   */
  maximumDays?: number;
  /**
   * Whether ranges containing unavailable dates may be selected.
   */
  allowNonContiguousRanges?: boolean;
  /**
   * Shortcut list rendered next to the calendars.
   */
  presets?: DatePickerPreset<DateRangeValue>[];
}

export interface DateTimePanelProps {
  /**
   * Time used when the user picks a date without an existing time. Defaults
   * to `00:00`.
   */
  defaultTime?: string;
  /**
   * `24` (default) or `12` hour clock.
   */
  hourCycle?: 12 | 24;
  /**
   * Label shown next to the time field. Defaults to `Time`.
   */
  timeLabel?: string;
}

export interface DateTimeRangePanelProps {
  /**
   * Time applied to a newly selected start date. Defaults to `00:00`.
   */
  defaultStartTime?: string;
  /**
   * Time applied to a newly selected end date. Defaults to `23:59`.
   */
  defaultEndTime?: string;
  /**
   * `24` (default) or `12` hour clock.
   */
  hourCycle?: 12 | 24;
  /**
   * Labels shown next to the start and end time fields.
   */
  startTimeLabel?: string;
  endTimeLabel?: string;
}

export type DatePickerDropdownMode = "date" | "datetime";

export type DateRangePickerDropdownMode = "date-range" | "datetime-range";
