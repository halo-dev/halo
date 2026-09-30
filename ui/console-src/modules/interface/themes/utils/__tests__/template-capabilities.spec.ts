import type { ThemeTemplateFile } from "@halo-dev/api-client";
import { describe, expect, it } from "vite-plus/test";
import {
  createTemplateDescriptions,
  ERROR_TEMPLATE_PATH,
  filterTemplatesByUsageType,
  getCustomTemplateContentTypeLabelKey,
  getUsageTypeLabelKey,
  matchesTemplateKeyword,
  summarizeTemplates,
} from "../template-capabilities";

const templates: ThemeTemplateFile[] = [
  {
    path: "post.html",
    usages: [{ type: "system", contentType: "post" }],
  },
  {
    path: "page.html",
    usages: [{ type: "system", contentType: "page" }],
  },
  {
    path: "layout.html",
    usages: [{ type: "layout" }],
  },
  {
    path: "post_custom.html",
    usages: [
      { type: "custom", contentType: "post", name: "Custom post" },
      { type: "other" },
    ],
  },
  {
    path: "partials/footer.html",
    usages: [{ type: "other" }],
  },
];

describe("template capabilities helpers", () => {
  it("summarizes templates per usage type", () => {
    const summary = summarizeTemplates(templates);
    expect(summary).toEqual([
      { type: "system", total: 2 },
      { type: "custom", total: 1 },
      { type: "layout", total: 1 },
      { type: "other", total: 2 },
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
          usages: [{ type: "custom", description: "For albums" }],
        },
        "ALBUM"
      )
    ).toBe(true);
    expect(matchesTemplateKeyword(templates[0], "missing")).toBe(false);
    expect(matchesTemplateKeyword(templates[0], "  ")).toBe(true);
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

describe("createTemplateDescriptions", () => {
  const t = (key: string) => `translated:${key}`;

  it("resolves descriptions for every known template path", () => {
    const map = createTemplateDescriptions(t);
    expect(map["archives.html"].description).toBe(
      "translated:core.theme.templates.pages.archives"
    );
    expect(map["author.html"].description).toBe(
      "translated:core.theme.templates.pages.author"
    );
    expect(map["error.html"].description).toBe(
      "translated:core.theme.templates.pages.error"
    );
    expect(map[ERROR_TEMPLATE_PATH].description).toBe(
      "translated:core.theme.templates.pages.error"
    );
  });

  it("takes routes from the backend-provided route patterns", () => {
    const routes = {
      "index.html": "/",
      "category.html": "/categories/{slug}",
      "post.html": "/archives/{slug}",
      "author.html": "/authors/{name}",
    };
    const map = createTemplateDescriptions(t, routes);
    expect(map["index.html"].route).toBe("/");
    expect(map["category.html"].route).toBe("/categories/{slug}");
    expect(map["post.html"].route).toBe("/archives/{slug}");
    expect(map["author.html"].route).toBe("/authors/{name}");
  });

  it("keeps verified fixed routes for pages outside the route rules", () => {
    const map = createTemplateDescriptions(t, {});
    expect(map["login.html"].route).toBe("/login");
    expect(map["signup.html"].route).toBe("/signup");
    expect(map["password-reset/email/send.html"].route).toBe(
      "/password-reset/email"
    );
  });

  it("leaves routes empty for pages without a fixed public url", () => {
    const map = createTemplateDescriptions(t, {});
    expect(map[ERROR_TEMPLATE_PATH].route).toBeUndefined();
    expect(map["logout.html"].route).toBeUndefined();
    expect(map["challenges/two-factor/totp.html"].route).toBeUndefined();
    expect(map["archives.html"].route).toBeUndefined();
  });
});
