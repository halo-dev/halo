import { afterEach, describe, expect, it, vi } from "vitest";
import { DateUtils } from "../date";

describe("DateUtils", () => {
  const dateUtils = new DateUtils();

  afterEach(() => {
    dateUtils.setLocale("en");
    vi.useRealTimers();
  });

  it.each(["es", "es-ES"])(
    "formats relative time in Spanish for %s",
    (locale) => {
      vi.useFakeTimers();
      vi.setSystemTime(new Date("2025-01-02T00:00:00Z"));
      dateUtils.setLocale(locale);

      expect(dateUtils.timeAgo("2025-01-01T00:00:00Z")).toBe("hace un día");
    }
  );
});
