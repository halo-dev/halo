import type { Theme } from "@halo-dev/api-client";
import { describe, expect, it } from "vite-plus/test";
import {
  getPageLayout,
  getPageLayoutDescriptionKey,
  getPageLayoutDiagnostic,
  getPageLayoutDotState,
  getPageLayoutLabelKey,
} from "../page-layout";

describe("page layout helpers", () => {
  it("returns state label and description keys", () => {
    expect(getPageLayoutLabelKey("SUPPORTED")).toBe(
      "core.theme.page_layout.status.supported"
    );
    expect(getPageLayoutDescriptionKey("MISSING")).toBe(
      "core.theme.page_layout.description.missing"
    );
    expect(getPageLayoutLabelKey(undefined)).toBe("core.common.text.none");
  });

  it("maps layout states to status dot states", () => {
    expect(getPageLayoutDotState("SUPPORTED")).toBe("success");
    expect(getPageLayoutDotState("MISSING")).toBe("warning");
    expect(getPageLayoutDotState("INVALID")).toBe("error");
    expect(getPageLayoutDotState(undefined)).toBe("default");
  });

  it("reads layout status and diagnostic from theme", () => {
    const theme = {
      status: {
        pageLayout: {
          state: "INVALID",
          reason: "LayoutTemplateProbeFailed",
          message: "Probe failed",
        },
      },
    } as Theme;

    expect(getPageLayout(theme)?.state).toBe("INVALID");
    expect(getPageLayoutDiagnostic(getPageLayout(theme))).toBe("Probe failed");
  });
});
