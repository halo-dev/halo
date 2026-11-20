import { parseDate } from "@internationalized/date";
import { describe, expect, it } from "vite-plus/test";
import {
  combineDateTime,
  formatDateString,
  formatDateTimeString,
  formatTimeString,
  isDateInRange,
  normalizeTimeString,
  parseDateRange,
  parseDateTimeRange,
  parseDateTimeString,
  parseDateString,
  parseTimeString,
  resolveDefaultPickerDate,
  serializeDateRange,
  serializeDateTimeRange,
  splitDateTime,
  toDateKey,
} from "../date-utils";

describe("date-utils", () => {
  it("parses dates and ignores the time part", () => {
    expect(parseDateString("2026-11-19")?.toString()).toBe("2026-11-19");
    expect(parseDateString("2026-11-19T08:30")?.toString()).toBe("2026-11-19");
    expect(formatDateString(parseDate("2026-11-19"))).toBe("2026-11-19");
  });

  it("returns undefined for invalid dates", () => {
    expect(parseDateString("not-a-date")).toBeUndefined();
    expect(parseDateString("")).toBeUndefined();
    expect(parseDateString(undefined)).toBeUndefined();
  });

  it("parses date time values to minute precision", () => {
    expect(parseDateTimeString("2026-11-19", "09:15")?.toString()).toBe(
      "2026-11-19T09:15:00"
    );
    expect(parseDateTimeString("2026-11-19T08:30:45")?.toString()).toBe(
      "2026-11-19T08:30:00"
    );
    expect(
      formatDateTimeString(parseDateTimeString("2026-11-19T08:30:45"))
    ).toBe("2026-11-19T08:30");
  });

  it("parses and formats times", () => {
    expect(parseTimeString("08:30:45")?.toString()).toBe("08:30:00");
    expect(formatTimeString(parseTimeString("08:30"))).toBe("08:30");
    expect(normalizeTimeString("not-a-time", "23:59")).toBe("23:59");
    expect(normalizeTimeString("08:30")).toBe("08:30");
  });

  it("combines and splits date time values", () => {
    const combined = combineDateTime(parseDate("2026-11-19"), "08:30");
    const split = splitDateTime("2026-11-19", "23:59");

    expect(combined?.toString()).toBe("2026-11-19T08:30:00");
    expect(split?.date.toString()).toBe("2026-11-19");
    expect(split?.time).toBe("23:59");
  });

  it("parses and serializes date ranges", () => {
    const range = parseDateRange({
      start: "2026-11-01",
      end: "invalid",
    });

    expect(range.start?.toString()).toBe("2026-11-01");
    expect(range.end).toBeUndefined();
    expect(
      serializeDateRange(parseDate("2026-11-01"), parseDate("2026-11-19"))
    ).toEqual({ start: "2026-11-01", end: "2026-11-19" });
    expect(serializeDateRange(parseDate("2026-11-01"), undefined)).toEqual({
      start: "2026-11-01",
    });
    expect(serializeDateRange(undefined, undefined)).toBeUndefined();
  });

  it("parses and serializes date time ranges", () => {
    const value = serializeDateTimeRange(
      { date: parseDate("2026-11-01"), time: "08:30" },
      { date: parseDate("2026-11-19"), time: "18:00" }
    );

    expect(value).toEqual({
      start: "2026-11-01T08:30",
      end: "2026-11-19T18:00",
    });

    const parsed = parseDateTimeRange(value, "00:00", "23:59");

    expect(parsed.start?.date.toString()).toBe("2026-11-01");
    expect(parsed.start?.time).toBe("08:30");
    expect(parsed.end?.date.toString()).toBe("2026-11-19");
    expect(parsed.end?.time).toBe("18:00");
  });

  it("fills missing times with the range defaults", () => {
    const parsed = parseDateTimeRange(
      { start: "2026-11-01", end: "2026-11-19" },
      "00:00",
      "23:59"
    );

    expect(parsed.start?.time).toBe("00:00");
    expect(parsed.end?.time).toBe("23:59");
  });

  it("checks range membership and date keys", () => {
    expect(toDateKey(parseDate("2026-11-19"))).toBe("2026-11-19");
    expect(
      isDateInRange(
        parseDate("2026-11-10"),
        parseDate("2026-11-01"),
        parseDate("2026-11-19")
      )
    ).toBe(true);
    expect(
      isDateInRange(
        parseDate("2026-11-20"),
        parseDate("2026-11-01"),
        parseDate("2026-11-19")
      )
    ).toBe(false);
    expect(isDateInRange(parseDate("2026-11-10"))).toBe(true);
  });

  it("falls back when resolving the initial month", () => {
    expect(resolveDefaultPickerDate(undefined, "2026-11-05").toString()).toBe(
      "2026-11-05"
    );
  });
});
