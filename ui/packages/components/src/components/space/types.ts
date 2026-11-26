export type SpaceSpacing = "xs" | "sm" | "md" | "lg" | "xl" | "2xl";
export type SpaceDirection = "row" | "column";
export type SpaceAlign = "start" | "end" | "center" | "stretch" | "baseline";
export type SpaceJustify =
  | "start"
  | "end"
  | "center"
  | "between"
  | "around"
  | "evenly";
/**
 * Resolved spacing used by the component.
 * A tuple is `[horizontal, vertical]`.
 */
export type SpaceGap = number | [number, number];
export type SpaceSpacingValue = SpaceSpacing | SpaceGap;

export const SpaceSpacingSize: Record<SpaceSpacing, number> = {
  xs: 10,
  sm: 12,
  md: 16,
  lg: 20,
  xl: 24,
  "2xl": 32,
};
