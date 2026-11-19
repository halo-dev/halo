import {
  CalendarDate,
  CalendarDateTime,
  Time,
  getLocalTimeZone,
  parseDate,
  parseDateTime,
  parseTime,
  toCalendarDate,
  toCalendarDateTime,
  today,
  type DateValue,
  type ZonedDateTime,
} from "@internationalized/date";
import type { DateRangeValue } from "./types";

export interface CalendarDateRange {
  start?: CalendarDate;
  end?: CalendarDate;
}

export interface CalendarDateTimeValue {
  date: CalendarDate;
  time: string;
}

const DATE_PREFIX = /^\d{4}-\d{2}-\d{2}/;

function pad(value: number, size = 2): string {
  return String(value).padStart(size, "0");
}

function getDatePart(value: string): string | undefined {
  const trimmed = value.trim();

  if (!trimmed) {
    return undefined;
  }

  return DATE_PREFIX.test(trimmed) ? trimmed.slice(0, 10) : trimmed;
}

/**
 * Parses `YYYY-MM-DD`. A date time string is accepted and truncated to its
 * date part. Invalid input returns `undefined`.
 */
export function parseDateString(value?: string): CalendarDate | undefined {
  if (!value) {
    return undefined;
  }

  const datePart = getDatePart(value);

  if (!datePart) {
    return undefined;
  }

  try {
    return parseDate(datePart);
  } catch {
    return undefined;
  }
}

export function formatDateString(value?: DateValue | null): string | undefined {
  return value ? value.toString().slice(0, 10) : undefined;
}

/**
 * Parses `YYYY-MM-DDTHH:mm`. Seconds and timezone suffixes are dropped, the
 * value is treated as local wall time. A date-only string is completed with
 * `defaultTime`.
 */
export function parseDateTimeString(
  value?: string,
  defaultTime = "00:00"
): CalendarDateTime | undefined {
  if (!value) {
    return undefined;
  }

  const trimmed = value.trim();

  if (!trimmed) {
    return undefined;
  }

  const normalized = /^\d{4}-\d{2}-\d{2}$/.test(trimmed)
    ? `${trimmed}T${defaultTime}`
    : trimmed.slice(0, 16);

  try {
    return parseDateTime(normalized).set({ second: 0, millisecond: 0 });
  } catch {
    return undefined;
  }
}

export function formatDateTimeString(
  value?: DateValue | ZonedDateTime | CalendarDateTime | null
): string | undefined {
  if (!value) {
    return undefined;
  }

  const dateTime = toCalendarDateTime(value);

  return `${pad(dateTime.year, 4)}-${pad(dateTime.month)}-${pad(
    dateTime.day
  )}T${pad(dateTime.hour)}:${pad(dateTime.minute)}`;
}

export function parseTimeString(value?: string): Time | undefined {
  if (!value) {
    return undefined;
  }

  try {
    return parseTime(value.trim()).set({ second: 0, millisecond: 0 });
  } catch {
    return undefined;
  }
}

/**
 * Resolves a time string, falling back to `fallback` and finally midnight.
 */
export function resolveTime(value?: string, fallback = "00:00"): Time {
  return (
    parseTimeString(value) ?? parseTimeString(fallback) ?? new Time(0, 0, 0, 0)
  );
}

export function formatTimeString(
  value?: { hour: number; minute: number } | null
): string | undefined {
  return value ? `${pad(value.hour)}:${pad(value.minute)}` : undefined;
}

/**
 * Resolves a time string to `HH:mm`, using `fallback` for invalid input.
 */
export function normalizeTimeString(
  value?: string,
  fallback = "00:00"
): string {
  return formatTimeString(resolveTime(value, fallback)) as string;
}

export function combineDateTime(
  date: CalendarDate,
  time: string | Time
): CalendarDateTime | undefined {
  const parsedTime = typeof time === "string" ? parseTimeString(time) : time;

  if (!parsedTime) {
    return undefined;
  }

  return toCalendarDateTime(date).set({
    hour: parsedTime.hour,
    minute: parsedTime.minute,
    second: 0,
    millisecond: 0,
  });
}

export function splitDateTime(
  value?: string,
  defaultTime = "00:00"
): CalendarDateTimeValue | undefined {
  const dateTime = parseDateTimeString(value, defaultTime);

  if (!dateTime) {
    return undefined;
  }

  return {
    date: toCalendarDate(dateTime),
    time: formatTimeString(dateTime) as string,
  };
}

export function todayCalendarDate(): CalendarDate {
  return today(getLocalTimeZone());
}

export function toCalendarDateValue(
  value?: DateValue | null
): CalendarDate | undefined {
  return value ? toCalendarDate(value) : undefined;
}

export function toDateKey(value: DateValue): string {
  return value.toString().slice(0, 10);
}

export function isDateInRange(
  date: DateValue,
  start?: DateValue,
  end?: DateValue
): boolean {
  const key = toDateKey(date);
  const startKey = start ? toDateKey(start) : undefined;
  const endKey = end ? toDateKey(end) : undefined;

  return (!startKey || key >= startKey) && (!endKey || key <= endKey);
}

export function parseDateRange(value?: DateRangeValue): CalendarDateRange {
  return {
    start: parseDateString(value?.start),
    end: parseDateString(value?.end),
  };
}

export function serializeDateRange(
  start?: CalendarDate | DateValue,
  end?: CalendarDate | DateValue
): DateRangeValue | undefined {
  const startValue = formatDateString(start ?? undefined);
  const endValue = formatDateString(end ?? undefined);

  if (!startValue && !endValue) {
    return undefined;
  }

  return {
    ...(startValue ? { start: startValue } : {}),
    ...(endValue ? { end: endValue } : {}),
  };
}

export function serializeDateTimeRange(
  start?: CalendarDateTimeValue,
  end?: CalendarDateTimeValue
): DateRangeValue | undefined {
  const startValue = start
    ? formatDateTimeString(combineDateTime(start.date, start.time))
    : undefined;
  const endValue = end
    ? formatDateTimeString(combineDateTime(end.date, end.time))
    : undefined;

  if (!startValue && !endValue) {
    return undefined;
  }

  return {
    ...(startValue ? { start: startValue } : {}),
    ...(endValue ? { end: endValue } : {}),
  };
}

export function parseDateTimeRange(
  value: DateRangeValue | undefined,
  defaultStartTime = "00:00",
  defaultEndTime = "23:59"
): { start?: CalendarDateTimeValue; end?: CalendarDateTimeValue } {
  return {
    start: splitDateTime(value?.start, defaultStartTime),
    end: splitDateTime(value?.end, defaultEndTime),
  };
}

export function resolveDefaultPickerDate(
  value?: string,
  defaultPickerValue?: string
): CalendarDate {
  return (
    parseDateString(value) ??
    parseDateString(defaultPickerValue) ??
    todayCalendarDate()
  );
}
