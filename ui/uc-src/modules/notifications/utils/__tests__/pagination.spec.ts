import { describe, expect, it } from "vitest";
import { getNextNotificationPageOffset } from "../pagination";

describe("getNextNotificationPageOffset", () => {
  it("returns the next page when nothing changed", () => {
    expect(
      getNextNotificationPageOffset(
        { nextOffset: 50, total: 120, removalsAtFetch: 0 },
        0
      )
    ).toBe(50);
  });

  it("moves back by the number of removals since the page was fetched", () => {
    expect(
      getNextNotificationPageOffset(
        { nextOffset: 100, total: 120, removalsAtFetch: 0 },
        3
      )
    ).toBe(50);
  });

  it("rounds down instead of skipping the page boundary", () => {
    expect(
      getNextNotificationPageOffset(
        { nextOffset: 100, total: 200, removalsAtFetch: 0 },
        1
      )
    ).toBe(50);

    expect(
      getNextNotificationPageOffset(
        { nextOffset: 150, total: 200, removalsAtFetch: 0 },
        1
      )
    ).toBe(100);
  });

  it("ignores removals that were already reflected in the page", () => {
    expect(
      getNextNotificationPageOffset(
        { nextOffset: 100, total: 200, removalsAtFetch: 10 },
        10
      )
    ).toBe(100);
  });

  it("returns undefined when the list is exhausted", () => {
    expect(
      getNextNotificationPageOffset(
        { nextOffset: 120, total: 120, removalsAtFetch: 0 },
        0
      )
    ).toBeUndefined();

    expect(
      getNextNotificationPageOffset(
        { nextOffset: 100, total: 50, removalsAtFetch: 0 },
        0
      )
    ).toBeUndefined();
  });

  it("never returns a negative offset", () => {
    expect(
      getNextNotificationPageOffset(
        { nextOffset: 50, total: 200, removalsAtFetch: 0 },
        75
      )
    ).toBe(0);
  });
});
