export type DrawerPlacement = "left" | "right" | "top" | "bottom";

export type DrawerSnapPoint = number | string;

const REM_BASE = 16;

const SNAP_POINT_PATTERN = /^([+-]?\d*\.?\d+)\s*(px|rem|em|vh|%)?$/;

export function isHorizontalPlacement(placement: DrawerPlacement): boolean {
  return placement === "left" || placement === "right";
}

/**
 * Resolve the configured size to a CSS value. Numbers are treated as pixels
 * and strings are passed through so consumers can use any CSS unit.
 */
export function resolveDrawerSize(
  size: number | string | undefined,
  fallback: string
): string {
  if (size === undefined || size === "") {
    return fallback;
  }

  return typeof size === "number" ? `${size}px` : size;
}

/**
 * Resolve a snap point to a pixel size against the current viewport.
 *
 * - `0 < number <= 1` is treated as a fraction of the viewport (1 === full).
 * - Numbers above 1 are treated as pixels.
 * - Strings support `px`, `rem`, `em`, `vh` and `%`.
 */
export function resolveSnapPointSize(
  point: DrawerSnapPoint,
  viewportSize: number,
  remBase = REM_BASE
): number {
  if (typeof point === "number") {
    if (!Number.isFinite(point) || point <= 0) {
      return 0;
    }

    if (point <= 1) {
      return point * viewportSize;
    }

    return Math.min(point, viewportSize);
  }

  const match = SNAP_POINT_PATTERN.exec(point.trim());

  if (!match) {
    return 0;
  }

  const amount = Number(match[1]);
  const unit = match[2] ?? "px";

  if (!Number.isFinite(amount) || amount <= 0) {
    return 0;
  }

  switch (unit) {
    case "rem":
    case "em":
      return amount * remBase;
    case "vh":
      return (amount / 100) * viewportSize;
    case "%":
      return (amount / 100) * viewportSize;
    default:
      return Math.min(amount, viewportSize);
  }
}

export interface DrawerSnapEntry {
  value: DrawerSnapPoint;
  height: number;
}

/**
 * Resolve and sort snap points by their pixel height. Points that resolve to
 * the same height are de-duplicated, which keeps index based navigation stable.
 */
export function normalizeSnapPoints(
  points: DrawerSnapPoint[] | undefined,
  viewportSize: number
): DrawerSnapEntry[] {
  const entries = (points ?? [])
    .map((value) => ({
      value,
      height: resolveSnapPointSize(value, viewportSize),
    }))
    .filter((entry) => entry.height > 0)
    .sort((a, b) => a.height - b.height);

  return entries.filter(
    (entry, index) => index === 0 || entry.height !== entries[index - 1].height
  );
}

export function findNearestSnapIndex(heights: number[], value: number): number {
  if (heights.length === 0) {
    return -1;
  }

  let nearest = 0;
  let nearestDistance = Math.abs(heights[0] - value);

  for (let index = 1; index < heights.length; index++) {
    const distance = Math.abs(heights[index] - value);

    if (distance < nearestDistance) {
      nearest = index;
      nearestDistance = distance;
    }
  }

  return nearest;
}

export interface SnapReleaseOptions {
  /** Snap point heights in pixels, sorted ascending. */
  heights: number[];
  currentIndex: number;
  /** Distance dragged towards dismissal, in pixels. */
  dragDistance: number;
  /** Drag velocity towards dismissal, in pixels per millisecond. */
  velocity: number;
  dismissible?: boolean;
  sequential?: boolean;
  /** Fraction of the smallest snap point below which the drawer dismisses. */
  dismissRatio?: number;
  /** Velocity in pixels per millisecond that dismisses the drawer. */
  velocityThreshold?: number;
}

export type SnapReleaseResult =
  | { dismiss: true }
  | { dismiss: false; index: number };

/**
 * Decide where a snap point drawer should settle after the pointer is released.
 */
export function resolveSnapRelease(
  options: SnapReleaseOptions
): SnapReleaseResult {
  const {
    heights,
    currentIndex,
    dragDistance,
    velocity,
    dismissible = true,
    sequential = false,
    dismissRatio = 0.5,
    velocityThreshold = 0.5,
  } = options;

  if (heights.length === 0) {
    return { dismiss: false, index: 0 };
  }

  const clampedCurrentIndex = Math.min(
    Math.max(currentIndex, 0),
    heights.length - 1
  );
  const currentHeight = heights[clampedCurrentIndex];

  if (dismissible && dragDistance > 0 && velocity >= velocityThreshold) {
    return { dismiss: true };
  }

  const candidate = currentHeight - dragDistance;
  const dismissLine = heights[0] * (1 - dismissRatio);

  if (dismissible && candidate < dismissLine) {
    return { dismiss: true };
  }

  let index = findNearestSnapIndex(heights, candidate);

  if (sequential) {
    if (index > clampedCurrentIndex) {
      index = clampedCurrentIndex + 1;
    } else if (index < clampedCurrentIndex) {
      index = clampedCurrentIndex - 1;
    }
  }

  return {
    dismiss: false,
    index: Math.min(Math.max(index, 0), heights.length - 1),
  };
}

export interface DragDismissOptions {
  /** Distance dragged towards dismissal, in pixels. */
  distance: number;
  /** Size of the drawer along the drag axis, in pixels. */
  size: number;
  /** Drag velocity towards dismissal, in pixels per millisecond. */
  velocity: number;
  /** Fraction of the drawer size that dismisses it. */
  threshold?: number;
  velocityThreshold?: number;
}

export function shouldDismissOnDrag(options: DragDismissOptions): boolean {
  const {
    distance,
    size,
    velocity,
    threshold = 0.25,
    velocityThreshold = 0.5,
  } = options;

  return distance > size * threshold || velocity >= velocityThreshold;
}

export interface ScrollDragBounds {
  scrollTop: number;
  clientHeight: number;
  scrollHeight: number;
}

/**
 * Content can only start a drag when it cannot scroll further in the drag
 * direction, so a scrollable body keeps receiving the gesture.
 */
export function canStartScrollDrag(
  element: ScrollDragBounds | undefined,
  placement: DrawerPlacement
): boolean {
  if (isHorizontalPlacement(placement)) {
    return true;
  }

  if (!element) {
    return false;
  }

  if (placement === "bottom") {
    return element.scrollTop <= 0;
  }

  return element.scrollTop + element.clientHeight >= element.scrollHeight - 1;
}
