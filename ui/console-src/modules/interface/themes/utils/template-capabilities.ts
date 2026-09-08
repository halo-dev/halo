import type {
  ThemeTemplateFile,
  ThemeTemplateUsage,
} from "@halo-dev/api-client";

export type TemplateState = ThemeTemplateFile["state"];
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
  unavailable: number;
}

/**
 * Groups template files by usage type. A file appears in every category its
 * usages declare, but only once per category (the backend already dedupes
 * usages by path).
 */
export function summarizeTemplates(
  templates: ThemeTemplateFile[]
): TemplateCategorySummary[] {
  return TEMPLATE_USAGE_TYPES.map((type) => {
    const files = filterTemplatesByUsageType(templates, type);
    return {
      type,
      total: files.length,
      unavailable: files.filter((file) => file.state !== "available").length,
    };
  });
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

export function getTemplateStateDotState(
  state: TemplateState
): "default" | "success" | "warning" | "error" {
  switch (state) {
    case "available":
      return "success";
    case "missing":
      return "warning";
    case "unreadable":
    case "invalid":
      return "error";
    default:
      return "default";
  }
}

export function getTemplateStateLabelKey(state: TemplateState): string {
  switch (state) {
    case "available":
      return "core.theme.templates.state.available";
    case "missing":
      return "core.theme.templates.state.missing";
    case "unreadable":
      return "core.theme.templates.state.unreadable";
    case "invalid":
      return "core.theme.templates.state.invalid";
    default:
      return "core.common.text.none";
  }
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
