import type { ComputedRef, InjectionKey } from "vue";

export type DescriptionSize = "xs" | "sm" | "md" | "lg";

export type DescriptionLayout = "horizontal" | "vertical" | "inline";

export type DescriptionAlign = "left" | "center" | "right";

export interface DescriptionContext {
  size: DescriptionSize;
  layout: DescriptionLayout;
  bordered: boolean;
  hover: boolean;
  divider: boolean;
  columns: number;
  separator: string;
  emptyText?: string;
  labelWidth?: string | number;
  align?: DescriptionAlign;
}

export const defaultDescriptionContext: DescriptionContext = {
  size: "md",
  layout: "horizontal",
  bordered: false,
  hover: true,
  divider: true,
  columns: 1,
  separator: ":",
};

export const descriptionContextKey: InjectionKey<
  ComputedRef<DescriptionContext>
> = Symbol("VDescriptionContext");
