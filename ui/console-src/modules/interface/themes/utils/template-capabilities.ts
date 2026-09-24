import type {
  ThemeTemplateFile,
  ThemeTemplateUsage,
} from "@halo-dev/api-client";

export type TemplateUsageType = ThemeTemplateUsage["type"];

export const TEMPLATE_USAGE_TYPES: TemplateUsageType[] = [
  "system",
  "custom",
  "layout",
  "other",
];

export interface TemplateCategorySummary {
  type: TemplateUsageType;
  total: number;
}

/**
 * Groups template files by usage type. A file appears in every category its
 * usages declare, but only once per category (the backend already dedupes
 * usages by path).
 */
export function summarizeTemplates(
  templates: ThemeTemplateFile[]
): TemplateCategorySummary[] {
  return TEMPLATE_USAGE_TYPES.map((type) => ({
    type,
    total: filterTemplatesByUsageType(templates, type).length,
  }));
}

export function filterTemplatesByUsageType(
  templates: ThemeTemplateFile[],
  type: TemplateUsageType
): ThemeTemplateFile[] {
  return templates.filter((file) =>
    file.usages.some((usage) => usage.type === type)
  );
}

export function matchesTemplateKeyword(
  file: ThemeTemplateFile,
  keyword: string
): boolean {
  const normalized = keyword.trim().toLowerCase();
  if (!normalized) {
    return true;
  }
  if (file.path.toLowerCase().includes(normalized)) {
    return true;
  }
  return file.usages.some(
    (usage) =>
      usage.name?.toLowerCase().includes(normalized) ||
      usage.description?.toLowerCase().includes(normalized)
  );
}

export function getUsageTypeLabelKey(type: TemplateUsageType): string {
  switch (type) {
    case "system":
      return "core.theme.templates.usage.system";
    case "custom":
      return "core.theme.templates.usage.custom";
    case "layout":
      return "core.theme.templates.usage.layout";
    case "other":
      return "core.theme.templates.usage.other";
    default:
      return "core.common.text.none";
  }
}

export function getCustomTemplateContentTypeLabelKey(
  contentType?: string
): string | undefined {
  switch (contentType) {
    case "post":
      return "core.theme.templates.content_type.post";
    case "category":
      return "core.theme.templates.content_type.category";
    case "page":
      return "core.theme.templates.content_type.page";
    default:
      return undefined;
  }
}

interface KnownTemplate {
  labelKey: string;
  /** Fixed public route for pages not covered by the site route rules. */
  fixedRoute?: string;
}

export const ERROR_TEMPLATE_PATH = "error/error.html";

/**
 * Semantic descriptions and route entries for every known system template,
 * keyed by the template path relative to the templates directory. Pages
 * without a fixed public URL (logout, setup, in-flight flows) intentionally
 * declare no route; files whose system usage is an error page (error.html,
 * error/404.html, ...) reuse the description of error/error.html.
 */
export const KNOWN_TEMPLATES: Record<string, KnownTemplate> = {
  "index.html": { labelKey: "core.theme.templates.pages.index" },
  "categories.html": { labelKey: "core.theme.templates.pages.categories" },
  "category.html": { labelKey: "core.theme.templates.pages.category" },
  "archives.html": { labelKey: "core.theme.templates.pages.archives" },
  "post.html": { labelKey: "core.theme.templates.pages.post" },
  "tags.html": { labelKey: "core.theme.templates.pages.tags" },
  "tag.html": { labelKey: "core.theme.templates.pages.tag" },
  "page.html": { labelKey: "core.theme.templates.pages.page" },
  "author.html": { labelKey: "core.theme.templates.pages.author" },
  "error.html": { labelKey: "core.theme.templates.pages.error" },
  [ERROR_TEMPLATE_PATH]: { labelKey: "core.theme.templates.pages.error" },
  "login.html": {
    labelKey: "core.theme.templates.pages.login",
    fixedRoute: "/login",
  },
  "signup.html": {
    labelKey: "core.theme.templates.pages.signup",
    fixedRoute: "/signup",
  },
  "logout.html": { labelKey: "core.theme.templates.pages.logout" },
  "setup.html": { labelKey: "core.theme.templates.pages.setup" },
  "complete_profile.html": {
    labelKey: "core.theme.templates.pages.complete_profile",
  },
  "login_oauth2_select.html": {
    labelKey: "core.theme.templates.pages.login_oauth2_select",
  },
  "password-reset/email/send.html": {
    labelKey: "core.theme.templates.pages.password_reset_send",
    fixedRoute: "/password-reset/email",
  },
  "password-reset/email/reset.html": {
    labelKey: "core.theme.templates.pages.password_reset_reset",
  },
  "challenges/two-factor/totp.html": {
    labelKey: "core.theme.templates.pages.two_factor",
  },
};

export interface TemplateDescription {
  description: string;
  route?: string;
}

/**
 * Builds the single lookup consumed by the UI: template path -> semantic
 * description and public route pattern. Routes for the built-in route
 * templates come from ThemeCapabilities.routes (already resolved from the
 * current route rules by the backend); only pages with a verified fixed
 * route carry one themselves. Call inside a computed so locale switches and
 * route rule changes stay reactive.
 */
export function createTemplateDescriptions(
  t: (key: string) => string,
  routes?: Record<string, string>
): Record<string, TemplateDescription> {
  const map: Record<string, TemplateDescription> = {};
  for (const [path, known] of Object.entries(KNOWN_TEMPLATES)) {
    map[path] = {
      description: t(known.labelKey),
      route: known.fixedRoute ?? routes?.[path],
    };
  }
  return map;
}
