import type { PageLayout, Theme } from "@halo-dev/api-client";

export type PageLayoutState = NonNullable<PageLayout["state"]>;
export type PageLayoutDotState = "default" | "success" | "warning" | "error";

export function getPageLayout(theme?: Theme): PageLayout | undefined {
  return theme?.status?.pageLayout;
}

export function getPageLayoutLabelKey(state?: PageLayoutState): string {
  switch (state) {
    case "SUPPORTED":
      return "core.theme.page_layout.status.supported";
    case "MISSING":
      return "core.theme.page_layout.status.missing";
    case "INVALID":
      return "core.theme.page_layout.status.invalid";
    default:
      return "core.common.text.none";
  }
}

export function getPageLayoutDescriptionKey(state?: PageLayoutState): string {
  switch (state) {
    case "SUPPORTED":
      return "core.theme.page_layout.description.supported";
    case "MISSING":
      return "core.theme.page_layout.description.missing";
    case "INVALID":
      return "core.theme.page_layout.description.invalid";
    default:
      return "core.common.text.none";
  }
}

export function getPageLayoutDotState(
  state?: PageLayoutState
): PageLayoutDotState {
  switch (state) {
    case "SUPPORTED":
      return "success";
    case "INVALID":
      return "error";
    case "MISSING":
      return "warning";
    default:
      return "default";
  }
}

export function getPageLayoutDiagnostic(
  layout?: PageLayout
): string | undefined {
  return layout?.message || layout?.reason;
}
