import { describe, expect, it } from "vite-plus/test";
import {
  findNearestSnapIndex,
  normalizeSnapPoints,
  resolveDrawerSize,
  resolveSnapPointSize,
  resolveSnapRelease,
  shouldDismissOnDrag,
} from "../drag";

describe("resolveDrawerSize", () => {
  it("falls back when no size is configured", () => {
    expect(resolveDrawerSize(undefined, "400px")).toBe("400px");
    expect(resolveDrawerSize("", "auto")).toBe("auto");
  });

  it("treats numbers as pixels and keeps CSS strings", () => {
    expect(resolveDrawerSize(480, "400px")).toBe("480px");
    expect(resolveDrawerSize("24rem", "400px")).toBe("24rem");
    expect(resolveDrawerSize("50%", "400px")).toBe("50%");
  });
});

describe("resolveSnapPointSize", () => {
  it.each([
    [0.5, 400],
    [1, 800],
    [320, 320],
    [1600, 800],
    ["50%", 400],
    ["40vh", 320],
    ["20rem", 320],
    ["200px", 200],
  ])("resolves %j against an 800px viewport", (point, expected) => {
    expect(resolveSnapPointSize(point, 800)).toBe(expected);
  });

  it("rejects invalid points", () => {
    expect(resolveSnapPointSize(0, 800)).toBe(0);
    expect(resolveSnapPointSize(-1, 800)).toBe(0);
    expect(resolveSnapPointSize("abc", 800)).toBe(0);
    expect(resolveSnapPointSize("0px", 800)).toBe(0);
  });
});

describe("normalizeSnapPoints", () => {
  it("sorts by height and drops duplicate heights", () => {
    expect(normalizeSnapPoints([1, 0.25, "50%", 0.25], 800)).toEqual([
      { value: 0.25, height: 200 },
      { value: "50%", height: 400 },
      { value: 1, height: 800 },
    ]);
  });

  it("returns an empty list without snap points", () => {
    expect(normalizeSnapPoints(undefined, 800)).toEqual([]);
  });
});

describe("findNearestSnapIndex", () => {
  it("returns the closest index", () => {
    expect(findNearestSnapIndex([200, 400, 800], 410)).toBe(1);
    expect(findNearestSnapIndex([200, 400, 800], 900)).toBe(2);
    expect(findNearestSnapIndex([200, 400, 800], 0)).toBe(0);
  });
});

describe("resolveSnapRelease", () => {
  const heights = [200, 400, 800];

  it("dismisses when released with enough velocity", () => {
    expect(
      resolveSnapRelease({
        heights,
        currentIndex: 0,
        dragDistance: 20,
        velocity: 1,
      })
    ).toEqual({ dismiss: true });
  });

  it("dismisses when dragged below the smallest snap point", () => {
    expect(
      resolveSnapRelease({
        heights,
        currentIndex: 0,
        dragDistance: 150,
        velocity: 0,
      })
    ).toEqual({ dismiss: true });
  });

  it("snaps to the nearest point", () => {
    expect(
      resolveSnapRelease({
        heights,
        currentIndex: 2,
        dragDistance: 450,
        velocity: 0,
      })
    ).toEqual({ dismiss: false, index: 1 });
  });

  it("does not dismiss when dismissal is disabled", () => {
    expect(
      resolveSnapRelease({
        heights,
        currentIndex: 0,
        dragDistance: 400,
        velocity: 2,
        dismissible: false,
      })
    ).toEqual({ dismiss: false, index: 0 });
  });

  it("moves at most one point when sequential snapping is enabled", () => {
    expect(
      resolveSnapRelease({
        heights,
        currentIndex: 0,
        dragDistance: -300,
        velocity: 0,
        sequential: true,
      })
    ).toEqual({ dismiss: false, index: 1 });
  });
});

describe("shouldDismissOnDrag", () => {
  it("dismisses past the size threshold", () => {
    expect(shouldDismissOnDrag({ distance: 120, size: 400, velocity: 0 })).toBe(
      true
    );
  });

  it("keeps the drawer open below the threshold", () => {
    expect(shouldDismissOnDrag({ distance: 40, size: 400, velocity: 0 })).toBe(
      false
    );
  });

  it("dismisses with enough velocity", () => {
    expect(
      shouldDismissOnDrag({ distance: 10, size: 400, velocity: 0.6 })
    ).toBe(true);
  });
});
