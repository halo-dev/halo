import type { ThemeTemplateFile } from "@halo-dev/api-client";
import { describe, expect, it } from "vite-plus/test";
import {
  filterTemplatesByUsageType,
  getCustomTemplateContentTypeLabelKey,
  getTemplateStateDotState,
  getTemplateStateLabelKey,
  getUsageTypeLabelKey,
  matchesTemplateKeyword,
  summarizeTemplates,
} from "../template-capabilities";

const templates: ThemeTemplateFile[] = [
  {
    path: "post.html",
    state: "available",
    usages: [{ type: "system", contentType: "post" }],
  },
  {
    path: "page.html",
    state: "missing",
    usages: [{ type: "system", contentType: "page" }],
  },
  {
    path: "layout.html",
    state: "available",
    usages: [{ type: "layout" }],
  },
  {
    path: "post_custom.html",
    state: "available",
    usages: [
      { type: "custom", contentType: "post", name: "Custom post" },
      { type: "other" },
    ],
  },
  {
    path: "partials/footer.html",
    state: "unreadable",
    usages: [{ type: "other" }],
  },
];

describe("template capabilities helpers", () => {
  it("summarizes templates per usage type with unavailable counts", () => {
    const summary = summarizeTemplates(templates);
    expect(summary).toEqual([
      { type: "system", total: 2, unavailable: 1 },
      { type: "custom", total: 1, unavailable: 0 },
      { type: "layout", total: 1, unavailable: 0 },
      { type: "other", total: 2, unavailable: 1 },
    ]);
  });

  it("keeps multi-usage files in every matching category only once", () => {
    expect(
      filterTemplatesByUsageType(templates, "other").map((f) => f.path)
    ).toEqual(["post_custom.html", "partials/footer.html"]);
    expect(filterTemplatesByUsageType(templates, "custom")).toHaveLength(1);
  });

  it("matches keyword against path, name and description", () => {
    expect(matchesTemplateKeyword(templates[0], "post")).toBe(true);
    expect(matchesTemplateKeyword(templates[3], "custom post")).toBe(true);
    expect(
      matchesTemplateKeyword(
        {
          path: "a.html",
          state: "available",
          usages: [{ type: "custom", description: "For albums" }],
        },
        "ALBUM"
      )
    ).toBe(true);
    expect(matchesTemplateKeyword(templates[0], "missing")).toBe(false);
    expect(matchesTemplateKeyword(templates[0], "  ")).toBe(true);
  });

  it("maps states to dot states and label keys", () => {
    expect(getTemplateStateDotState("available")).toBe("success");
    expect(getTemplateStateDotState("missing")).toBe("warning");
    expect(getTemplateStateDotState("unreadable")).toBe("error");
    expect(getTemplateStateDotState("invalid")).toBe("error");
    expect(getTemplateStateLabelKey("missing")).toBe(
      "core.theme.templates.state.missing"
    );
  });

  it("maps usage types and custom content types to label keys", () => {
    expect(getUsageTypeLabelKey("system")).toBe(
      "core.theme.templates.usage.system"
    );
    expect(getCustomTemplateContentTypeLabelKey("post")).toBe(
      "core.theme.templates.content_type.post"
    );
    expect(getCustomTemplateContentTypeLabelKey("unknown")).toBeUndefined();
  });
});
