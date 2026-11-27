import type { SkeletonSize } from "./types";

export function toCssSize(value: SkeletonSize | undefined) {
  if (value === undefined) {
    return undefined;
  }

  return typeof value === "number" ? `${value}px` : value;
}
